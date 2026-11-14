package by.lab.refund;
import java.util.*;
import org.springframework.stereotype.Service;
@Service
public class RefundService {
 private final RefundRepository repository;
 public RefundService(RefundRepository repository) { this.repository = repository; }
 public List<Map<String,Object>> list() { return repository.findAll(); }
 public Map<String,Object> find(String id) { return repository.findById(id).orElse(null); }
}
