package by.lab.identity;
import java.util.*;
import org.springframework.stereotype.Service;
@Service
public class IdentityService {
 private final IdentityRepository repository;
 public IdentityService(IdentityRepository repository) { this.repository = repository; }
 public List<Map<String,Object>> list() { return repository.findAll(); }
 public Map<String,Object> find(String id) { return repository.findById(id).orElse(null); }
}
