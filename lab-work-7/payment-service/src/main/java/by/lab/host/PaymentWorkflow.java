package by.lab.host;
import by.lab.payment.*;
import by.lab.contract.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.*;
import io.grpc.stub.MetadataUtils;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
@Component public class PaymentWorkflow {
 private final PaymentRepository repository; private final PaymentService payment; private final JdbcTemplate db; private final ObjectMapper json=new ObjectMapper();
 public PaymentWorkflow(PaymentRepository repository,PaymentService payment,JdbcTemplate db) { this.repository=repository;this.payment=payment;this.db=db; }
 @Transactional public void start(String id,String target) {
  if(!Set.of("SUCCESS","CANCELLED").contains(target)) throw new IllegalArgumentException("Unsupported status");
  var p=repository.lockById(id).orElseThrow(() -> new IllegalArgumentException("Payment not found"));
  if(!"CREATED".equals(p.get("status"))) throw new IllegalArgumentException("Only a CREATED payment can change status");
  var rows=db.queryForList("select target_status,state from payment.operations where payment_id=?",id);
  if(!rows.isEmpty() && "PENDING".equals(rows.get(0).get("state")) && !target.equals(rows.get(0).get("target_status"))) throw new IllegalArgumentException("Another payment operation is pending");
  if("SUCCESS".equals(target) && (rows.isEmpty() || !"PENDING".equals(rows.get(0).get("state")))) {
   ManagedChannel c=ManagedChannelBuilder.forAddress(System.getenv().getOrDefault("PAYMENTMETHOD_HOST","paymentmethod-service"),9090).usePlaintext().build();
   try { var check=GrpcRetry.call("paymentmethod","validate",() -> PaymentMethodServiceGrpc.newBlockingStub(c).withDeadlineAfter(4,TimeUnit.SECONDS).validatePaymentMethod(ValidatePaymentMethodRequest.newBuilder().setPaymentMethodId(p.get("payment_method_id").toString()).setClientId(p.get("client_id").toString()).build())); if(!check.getValid()) throw new PaymentRejectedException("Payment method is inactive or does not belong to the client"); } finally { c.shutdown(); }
  }
  db.update("insert into payment.operations(payment_id,target_status,state) values (?,?,'PENDING') on conflict(payment_id) do update set target_status=excluded.target_status,state='PENDING',last_error=null",id,target);
 }
 @Transactional public Map<String,Object> execute(String id) {
  var p=repository.lockById(id).orElseThrow(() -> new IllegalArgumentException("Payment not found")); var op=db.queryForMap("select target_status,state from payment.operations where payment_id=? for update",id);
  if("DONE".equals(op.get("state"))) return p; if(!"PENDING".equals(op.get("state"))) throw new IllegalArgumentException("Operation is not pending"); String status=op.get("target_status").toString();
  if("SUCCESS".equals(status)) debit(p);
  Map<String,Object> result=payment.transition(id,status); event(status.equals("SUCCESS")?"PaymentConfirmed":"PaymentCancelled",result); event("PaymentStatusChanged",result); db.update("update payment.operations set state='DONE' where payment_id=?",id); return result;
 }
 private void debit(Map<String,Object> p) {
  String path=System.getenv("ACCOUNT_SERVICE_SECRET_FILE"); if(path==null) throw new IllegalStateException("Account service secret is not configured"); String secret;
  try { secret=Files.readString(Path.of(path)).trim(); } catch(Exception e) { throw new IllegalStateException("Cannot read account service secret",e); }
  if(secret.isBlank()) throw new IllegalStateException("Account service secret is empty");
  ManagedChannel c=ManagedChannelBuilder.forAddress(System.getenv().getOrDefault("ACCOUNT_HOST","account-service"),9090).usePlaintext().build();
  try { Metadata h=new Metadata(); h.put(Metadata.Key.of("x-payment-service-secret",Metadata.ASCII_STRING_MARSHALLER),secret); var request=DebitAccountRequest.newBuilder().setPaymentId(p.get("id").toString()).setOwnerId(p.get("client_id").toString()).setCurrency(p.get("currency").toString()).setAmount(p.get("amount").toString()).build(); GrpcRetry.call("account","debit",() -> AccountServiceGrpc.newBlockingStub(c).withInterceptors(MetadataUtils.newAttachHeadersInterceptor(h)).withDeadlineAfter(8,TimeUnit.SECONDS).debitAccount(request)); }
  catch(StatusRuntimeException e) { if(e.getStatus().getCode()==Status.Code.INVALID_ARGUMENT) throw new PaymentRejectedException("Account unavailable or insufficient balance"); throw new IllegalStateException("Account service unavailable",e); } finally { c.shutdown(); }
 }
 private void event(String type,Map<String,Object> p) { try { String id=UUID.randomUUID().toString(); Map<String,Object> e=new LinkedHashMap<>(); e.put("eventId",id);e.put("eventType",type);e.put("paymentId",p.get("id"));e.put("clientId",p.get("client_id"));e.put("merchantId",p.get("merchant_id"));e.put("amount",p.get("amount"));e.put("currency",p.get("currency"));e.put("status",p.get("status"));e.put("timestamp",Instant.now().toString()); db.update("insert into payment.outbox(event_id,payment_id,payload) values (?,?,?)",id,p.get("id"),json.writeValueAsString(e)); } catch(Exception e) { throw new IllegalStateException("Cannot save payment event",e); } }
 @Transactional public void reject(String id,String error) { db.update("update payment.operations set state='REJECTED',last_error=? where payment_id=? and state='PENDING'",error,id); }
 public List<String> pending() { return db.queryForList("select payment_id from payment.operations where state='PENDING' order by payment_id",String.class); }
}
