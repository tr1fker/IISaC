package by.lab.payment;
import java.util.*;
import org.springframework.stereotype.Service;
@Service
public class PaymentService {
 private final PaymentRepository repository;
 public PaymentService(PaymentRepository repository) { this.repository = repository; }
 public List<Map<String,Object>> list() { return repository.findAll(); }
 public Map<String,Object> find(String id) { return repository.findById(id).orElse(null); }
}
