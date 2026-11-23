package by.lab.identity;
import by.lab.contracts.Models.User;
import java.util.*;
import org.springframework.stereotype.Service;
@Service public class IdentityService {
 private final IdentityRepository repository;
 public IdentityService(IdentityRepository repository) { this.repository=repository; }
 public List<User> list() { return repository.findAll(); }
 public User find(String id) { return repository.findById(id).orElse(null); }
}
