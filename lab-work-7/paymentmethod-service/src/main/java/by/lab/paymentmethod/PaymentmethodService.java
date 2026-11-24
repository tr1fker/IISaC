package by.lab.paymentmethod;
import java.util.*;
import org.springframework.stereotype.Service;
@Service
public class PaymentmethodService {
 private final PaymentmethodRepository repository;
 public PaymentmethodService(PaymentmethodRepository repository) { this.repository = repository; }
 public List<Map<String,Object>> list() { return repository.findAll(); }
 public Map<String,Object> find(String id) { return repository.findById(id).orElse(null); }
 public Map<String,Object> add(String clientId,String type,String lastFour) { if(clientId==null || clientId.isBlank() || type==null || type.isBlank() || lastFour==null || !lastFour.matches("[0-9]{4}")) throw new IllegalArgumentException("Client, type and four card digits are required"); String id=UUID.randomUUID().toString(); repository.insert(id,clientId,type.trim().toUpperCase(Locale.ROOT),lastFour); return find(id); }
 public boolean remove(String id) { if(repository.deactivate(id)==0) throw new IllegalArgumentException("Payment method does not exist or is inactive"); return true; }
}
