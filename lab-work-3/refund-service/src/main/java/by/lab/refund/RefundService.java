package by.lab.refund;
import by.lab.contracts.Models.*;
import java.util.*;
import org.springframework.stereotype.Service;
@Service public class RefundService {
 private final RefundRepository repository;
 public RefundService(RefundRepository repository) { this.repository=repository; }
 public List<Refund> list() { return repository.findAll(); }
 public Refund find(String id) { return repository.findById(id).orElse(null); }
 public List<Refund> search(RefundFilter f) { return repository.findAll().stream().filter(row -> f.paymentId()==null || f.paymentId().equals(row.paymentId())).toList(); }
}
