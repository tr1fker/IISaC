package by.lab.host;
import by.lab.refund.RefundRepository;
import by.lab.contract.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
@Component public class RefundOperations {
 private final RefundRepository refunds; private final JdbcTemplate db; private final ObjectMapper json=new ObjectMapper();
 public RefundOperations(RefundRepository refunds,JdbcTemplate db) { this.refunds=refunds; this.db=db; }
 @Transactional public Map<String,Object> create(Map<String,Object> input) {
  String paymentId=Objects.toString(input.get("paymentId"),""); BigDecimal amount;
  try { amount=new BigDecimal(input.get("amount").toString()); } catch(Exception e) { throw new IllegalArgumentException("Invalid refund amount"); }
  if(paymentId.isBlank() || amount.signum()<=0 || amount.scale()>2) throw new IllegalArgumentException("Invalid refund input");
  ManagedChannel channel=ManagedChannelBuilder.forAddress(System.getenv().getOrDefault("PAYMENT_HOST","payment-service"),9090).usePlaintext().build(); GetPaymentResponse payment;
  try { payment=PaymentServiceGrpc.newBlockingStub(channel).withDeadlineAfter(4,TimeUnit.SECONDS).getPayment(GetPaymentRequest.newBuilder().setPaymentId(paymentId).build()); }
  catch(StatusRuntimeException e) { if(e.getStatus().getCode()==Status.Code.NOT_FOUND) throw new IllegalArgumentException("Payment not found"); throw new IllegalStateException("Payment service unavailable",e); } finally { channel.shutdown(); }
  if(!"SUCCESS".equals(payment.getStatus())) throw new IllegalArgumentException("Only a successful payment can be refunded");
  refunds.lockPayment(paymentId); if(refunds.total(paymentId).add(amount).compareTo(new BigDecimal(payment.getAmountDecimal()))>0) throw new IllegalArgumentException("Refund exceeds remaining amount");
  String id=UUID.randomUUID().toString(),eventId=UUID.randomUUID().toString(),created=Instant.now().toString(); refunds.insert(id,paymentId,amount,payment.getCurrency(),created);
  Map<String,Object> event=new LinkedHashMap<>(); event.put("eventId",eventId); event.put("eventType","RefundCreated"); event.put("refundId",id); event.put("paymentId",paymentId); event.put("clientId",payment.getClientId()); event.put("merchantId",payment.getMerchantId()); event.put("amount",amount); event.put("currency",payment.getCurrency()); event.put("timestamp",created);
  try { db.update("insert into refund.outbox(event_id,payment_id,payload) values (?,?,?)",eventId,paymentId,json.writeValueAsString(event)); } catch(Exception e) { throw new IllegalStateException("Cannot save refund event",e); }
  return refunds.findById(id).orElseThrow();
 }
}
