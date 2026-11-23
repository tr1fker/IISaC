package by.lab.transaction;
import java.util.*;
import org.springframework.stereotype.Service;
@Service
public class TransactionService {
 private final TransactionRepository repository;
 public TransactionService(TransactionRepository repository) { this.repository = repository; }
 public List<Map<String,Object>> list() { return repository.findAll(); }
 public Map<String,Object> find(String id) { return repository.findById(id).orElse(null); }
 public void record(Map<String,Object> payment,String type) { repository.insert(payment.get("id").toString()+":"+type,payment.get("id").toString(),payment.get("client_id").toString(),new java.math.BigDecimal(payment.get("amount").toString()),payment.get("currency").toString(),type,java.time.Instant.now().toString()); }
 private boolean matches(Map<String,Object> row,String key,Object value) { return value==null || value.toString().equals(Objects.toString(row.get(key),"")); }
 public List<Map<String,Object>> search(Map<String,Object> f) { return repository.findAll().stream().filter(row -> matches(row,"payment_id",f.get("paymentId")) && matches(row,"client_id",f.get("clientId"))).toList(); }
}
