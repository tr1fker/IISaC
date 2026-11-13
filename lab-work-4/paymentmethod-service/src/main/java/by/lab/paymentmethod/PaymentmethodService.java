package by.lab.paymentmethod;
import java.util.*;
import org.springframework.stereotype.Service;
@Service
public class PaymentmethodService {
 private final PaymentmethodRepository repository;
 public PaymentmethodService(PaymentmethodRepository repository) { this.repository = repository; }
 public List<Map<String,Object>> list() { return repository.findAll(); }
 public Map<String,Object> find(String id) { return repository.findById(id).orElse(null); }
}
