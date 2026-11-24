package by.lab.host;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
@Component public class Events {
 private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(Events.class);
 private final JdbcTemplate db; private final KafkaTemplate<String,String> kafka; private final TransactionTemplate tx; private final ObjectMapper json=new ObjectMapper(); private final String name;
 public Events(JdbcTemplate db,KafkaTemplate<String,String> kafka,TransactionTemplate tx,@Value("${service.name}") String name) { this.db=db;this.kafka=kafka;this.tx=tx;this.name=name; }
 @Scheduled(fixedDelay=2000) public void publish() {
  if(!Set.of("payment","refund").contains(name)) return;
  for(var row:db.queryForList("select event_id,payment_id,payload from "+name+".outbox where published=false order by created_at,event_id limit 100")) {
   try { kafka.send(name+"-events",row.get("payment_id").toString(),row.get("payload").toString()).get(10,TimeUnit.SECONDS); db.update("update "+name+".outbox set published=true where event_id=?",row.get("event_id")); log.info("Published event={} payment={}",row.get("event_id"),row.get("payment_id")); }
   catch(Exception e) { log.error("Outbox publication failed; will retry",e); break; }
  }
 }
 @KafkaListener(topics={"payment-events","refund-events"}) public void consume(String value) {
  if(!Set.of("transaction","notification","analytics","account").contains(name)) return;
  try { Map<String,Object> e=json.readValue(value,new TypeReference<Map<String,Object>>() {}); if(name.equals("account")) { if("RefundCreated".equals(e.get("eventType"))) { validateRefund(e); tx.executeWithoutResult(s -> creditRefund(e)); } return; } tx.executeWithoutResult(s -> apply(e)); }
  catch(com.fasterxml.jackson.core.JsonProcessingException | IllegalArgumentException e) { throw new InvalidKafkaEventException("Invalid Kafka event",e); }
  catch(Exception e) { throw new IllegalStateException("Failed to process Kafka event",e); }
 }
 @SuppressWarnings("unchecked") private void validateRefund(Map<String,Object> e) throws Exception {
  String refundId=Objects.toString(e.get("refundId"),""),paymentId=Objects.toString(e.get("paymentId"),"");
  io.grpc.ManagedChannel refunds=io.grpc.ManagedChannelBuilder.forAddress(System.getenv().getOrDefault("REFUND_HOST","refund-service"),9090).usePlaintext().build(), payments=io.grpc.ManagedChannelBuilder.forAddress(System.getenv().getOrDefault("PAYMENT_HOST","payment-service"),9090).usePlaintext().build();
  try {
   String raw=by.lab.contract.DataServiceGrpc.newBlockingStub(refunds).withDeadlineAfter(4,TimeUnit.SECONDS).call(by.lab.contract.DataRequest.newBuilder().setOperation("find").setId(refundId).build()).getPayloadJson(); Map<String,Object> refund=json.readValue(raw,Map.class);
   var payment=by.lab.contract.PaymentServiceGrpc.newBlockingStub(payments).withDeadlineAfter(4,TimeUnit.SECONDS).getPayment(by.lab.contract.GetPaymentRequest.newBuilder().setPaymentId(paymentId).build());
   if(refund==null || !"SUCCESS".equals(payment.getStatus()) || !paymentId.equals(refund.get("payment_id")) || !"SUCCESS".equals(refund.get("status")) || !payment.getClientId().equals(e.get("clientId")) || !payment.getMerchantId().equals(e.get("merchantId")) || !payment.getCurrency().equals(e.get("currency")) || !payment.getCurrency().equals(refund.get("currency")) || new java.math.BigDecimal(refund.get("amount").toString()).compareTo(new java.math.BigDecimal(e.get("amount").toString()))!=0) throw new IllegalArgumentException("Refund event does not match stored data");
  } catch(io.grpc.StatusRuntimeException rpcError) { if(rpcError.getStatus().getCode()==io.grpc.Status.Code.NOT_FOUND || rpcError.getStatus().getCode()==io.grpc.Status.Code.INVALID_ARGUMENT) throw new IllegalArgumentException("Refund references nonexistent data",rpcError); throw rpcError; }
  finally { refunds.shutdown();payments.shutdown(); }
 }
 private void creditRefund(Map<String,Object> e) {
  int inserted=db.update("insert into account.processed_refunds(refund_id) values (?) on conflict do nothing",e.get("refundId")); if(inserted==0) return;
  if(db.update("update account.accounts set balance=balance+? where owner_id=? and currency=? and status='ACTIVE'",e.get("amount"),e.get("clientId"),e.get("currency"))!=1) throw new IllegalStateException("Refund account unavailable");
 }
 private void apply(Map<String,Object> e) {
  String id=e.get("eventId").toString(),type=e.get("eventType").toString(); log.info("Processing event={} type={} service={}",id,type,name);
  int inserted=db.update("insert into "+name+".processed_events(event_id) values (?) on conflict do nothing",id); if(inserted==0) return;
  if(name.equals("transaction") && Set.of("PaymentConfirmed","PaymentCancelled","RefundCreated").contains(type)) {
   String kind=type.equals("RefundCreated")?"REFUND":type.equals("PaymentCancelled")?"CANCELLATION":"PAYMENT";
   db.update("insert into transaction.transactions(id,payment_id,client_id,amount,currency,type,created_at) values (?,?,?,?,?,?,?)",id,e.get("paymentId"),e.get("clientId"),e.get("amount"),e.get("currency"),kind,e.get("timestamp"));
  }
  if(name.equals("notification") && Set.of("PaymentConfirmed","PaymentCancelled","RefundCreated").contains(type)) db.update("insert into notification.notifications(id,recipient_id,message,status,created_at) values (?,?,?,'CREATED',?)",id,e.get("clientId"),type+": "+e.get("paymentId"),e.get("timestamp"));
  if(!name.equals("analytics")) return;
  if(Set.of("PaymentCreated","PaymentConfirmed","PaymentCancelled","PaymentStatusChanged").contains(type)) {
   String status=type.equals("PaymentCreated")?"CREATED":type.equals("PaymentConfirmed")?"SUCCESS":type.equals("PaymentCancelled")?"CANCELLED":Objects.toString(e.get("status"),"CREATED");
   db.update("insert into analytics.payment_state(payment_id,merchant_id,currency,amount,status) values (?,?,?,?,?) on conflict(payment_id) do update set status=excluded.status",e.get("paymentId"),e.get("merchantId"),e.get("currency"),e.get("amount"),status);
  }
  if(type.equals("PaymentConfirmed") || type.equals("RefundCreated")) {
   String day=e.get("timestamp").toString().substring(0,10),key=e.get("merchantId")+":"+e.get("currency")+":"+day;
   if(type.equals("PaymentConfirmed")) db.update("insert into analytics.payment_statistics(id,merchant_id,period_from,period_to,payment_count,total_amount,refund_amount,revenue) values (?,?,?,?,1,?,0,?) on conflict(id) do update set payment_count=analytics.payment_statistics.payment_count+1,total_amount=analytics.payment_statistics.total_amount+excluded.total_amount,revenue=analytics.payment_statistics.revenue+excluded.revenue",key,e.get("merchantId"),day,day,e.get("amount"),e.get("amount"));
   else db.update("insert into analytics.payment_statistics(id,merchant_id,period_from,period_to,payment_count,total_amount,refund_amount,revenue) values (?,?,?,?,0,0,?,-?) on conflict(id) do update set refund_amount=analytics.payment_statistics.refund_amount+excluded.refund_amount,revenue=analytics.payment_statistics.revenue+excluded.revenue",key,e.get("merchantId"),day,day,e.get("amount"),e.get("amount"));
  }
 }
}
