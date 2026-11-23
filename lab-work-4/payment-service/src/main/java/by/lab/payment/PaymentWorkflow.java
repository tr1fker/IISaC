package by.lab.payment;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
@Component public class PaymentWorkflow {
 private final PaymentRepository payments; private final PaymentParticipants participants; private final JdbcTemplate db;
 public PaymentWorkflow(PaymentRepository payments,PaymentParticipants participants,JdbcTemplate db) { this.payments=payments; this.participants=participants; this.db=db; }
 @Transactional public void start(String id,String target) {
  if(!Set.of("SUCCESS","CANCELLED").contains(target)) throw new IllegalArgumentException("Unsupported payment status");
  var payment=payments.lockById(id).orElseThrow(() -> new IllegalArgumentException("Payment not found"));
  if(!"CREATED".equals(payment.get("status"))) throw new IllegalArgumentException("Only a CREATED payment can change status");
  var operations=db.queryForList("select target_status,state from payment.operations where payment_id=?",id);
  if(!operations.isEmpty() && "PENDING".equals(operations.get(0).get("state")) && !target.equals(operations.get(0).get("target_status"))) throw new IllegalArgumentException("Another payment operation is pending");
  if("SUCCESS".equals(target) && (operations.isEmpty() || !"PENDING".equals(operations.get(0).get("state")))) {
   var method=participants.paymentMethod(payment.get("payment_method_id").toString());
   if(method==null || !"ACTIVE".equals(method.get("status")) || !payment.get("client_id").equals(method.get("client_id"))) throw new PaymentRejectedException("Payment method is inactive or does not belong to the client");
  }
  db.update("insert into payment.operations(payment_id,target_status,state) values (?,?,'PENDING') on conflict(payment_id) do update set target_status=excluded.target_status,state='PENDING',last_error=null",id,target);
 }
 @Transactional public Map<String,Object> execute(String id) {
  var payment=payments.lockById(id).orElseThrow(() -> new IllegalArgumentException("Payment not found"));
  var op=db.queryForMap("select target_status,state from payment.operations where payment_id=? for update",id);
  if("DONE".equals(op.get("state"))) return payment;
  if(!"PENDING".equals(op.get("state"))) throw new IllegalArgumentException("Operation is not pending");
  String status=op.get("target_status").toString();
  if("SUCCESS".equals(status)) participants.debit(id,payment.get("client_id").toString(),payment.get("currency").toString(),new BigDecimal(payment.get("amount").toString()));
  participants.record(payment,"SUCCESS".equals(status)?"PAYMENT":"CANCELLATION"); participants.notifyPayment(payment,status);
  if(payments.transition(id,status)!=1) throw new IllegalStateException("Payment status changed during operation");
  db.update("update payment.operations set state='DONE' where payment_id=?",id); return payments.findById(id).orElseThrow();
 }
 @Transactional public void reject(String id,String message) { db.update("update payment.operations set state='REJECTED',last_error=? where payment_id=? and state='PENDING'",message,id); }
 public List<String> pending() { return db.queryForList("select payment_id from payment.operations where state='PENDING' order by payment_id",String.class); }
}
