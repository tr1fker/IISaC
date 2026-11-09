package by.lab.transaction;
import java.util.*;
import org.springframework.stereotype.Service;
@Service
public class TransactionService {
 private final TransactionRepository repository;
 public TransactionService(TransactionRepository repository) { this.repository = repository; }
 public List<Map<String,Object>> list() { return repository.findAll(); }
 public Map<String,Object> find(String id) { return repository.findById(id).orElse(null); }
}
