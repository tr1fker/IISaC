package by.lab.paymentmethod;
import by.lab.contracts.Models.*;
import java.util.*;
import org.springframework.stereotype.Service;
@Service public class PaymentmethodService {
 private final PaymentmethodRepository repository;
 public PaymentmethodService(PaymentmethodRepository repository) { this.repository=repository; }
 public List<PaymentMethod> list() { return repository.findAll(); }
 public PaymentMethod find(String id) { return repository.findById(id).orElse(null); }
 public List<PaymentMethod> search(MethodFilter f) { return repository.findAll().stream().filter(row -> f.clientId()==null || f.clientId().equals(row.clientId())).toList(); }
}
