package by.lab.refund;
import java.util.*;
import org.springframework.stereotype.Service;
@Service
public class RefundService {
 private final RefundRepository repository;
 private final RefundParticipants participants;
 public RefundService(RefundRepository repository,RefundParticipants participants) { this.repository=repository; this.participants=participants; }
 public List<Map<String,Object>> list() { return repository.findAll(); }
 public Map<String,Object> find(String id) { return repository.findById(id).orElse(null); }
 @org.springframework.transaction.annotation.Transactional
 public Map<String,Object> create(String paymentId,java.math.BigDecimal amount) {
  if(paymentId==null || paymentId.isBlank() || amount==null || amount.signum()<=0 || amount.scale()>2) throw new IllegalArgumentException("Invalid refund amount or payment");
  Map<String,Object> payment=participants.payment(paymentId);
  if(payment==null || !"SUCCESS".equals(payment.get("status"))) throw new IllegalArgumentException("Only a successful payment can be refunded");
  repository.lockPayment(paymentId);
  java.math.BigDecimal original=new java.math.BigDecimal(payment.get("amount").toString());
  if(repository.totalForPayment(paymentId).add(amount).compareTo(original)>0) throw new IllegalArgumentException("Refund exceeds the remaining payment amount");
  String id=UUID.randomUUID().toString(); repository.insert(id,paymentId,amount,payment.get("currency").toString(),java.time.Instant.now().toString()); return find(id);
 }
 private boolean matches(Map<String,Object> row,String key,Object value) { return value==null || value.toString().equals(Objects.toString(row.get(key),"")); }
 public List<Map<String,Object>> search(Map<String,Object> f) { return repository.findAll().stream().filter(row -> matches(row,"payment_id",f.get("paymentId"))).toList(); }
}
