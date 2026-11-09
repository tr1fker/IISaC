package by.lab.account;
import java.util.*;
import org.springframework.stereotype.Service;
@Service
public class AccountService {
 private final AccountRepository repository;
 public AccountService(AccountRepository repository) { this.repository = repository; }
 public List<Map<String,Object>> list() { return repository.findAll(); }
 public Map<String,Object> find(String id) { return repository.findById(id).orElse(null); }
}
