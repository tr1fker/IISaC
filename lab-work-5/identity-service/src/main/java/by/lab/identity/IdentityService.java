package by.lab.identity;
import java.util.*;
import org.springframework.stereotype.Service;
@Service
public class IdentityService {
 private final IdentityRepository repository;
 public IdentityService(IdentityRepository repository) { this.repository = repository; }
 public List<Map<String,Object>> list() { return repository.findAll(); }
 public Map<String,Object> find(String id) { return repository.findById(id).orElse(null); }
 public Map<String,Object> findByEmail(String email) { return repository.findByEmail(email).orElse(null); }
 public Map<String,Object> register(String id,String name,String email) {
  if(id==null || id.isBlank() || name==null || name.isBlank() || email==null || email.isBlank()) throw new IllegalArgumentException("Invalid registration");
  Map<String,Object> existing=find(id); if(existing!=null) { if(email.equals(existing.get("email")) && "CLIENT".equals(existing.get("role"))) return existing; throw new IllegalArgumentException("User already exists"); }
  try { repository.insert(id,name,email); } catch(org.springframework.dao.DuplicateKeyException ex) { throw new IllegalArgumentException("Email already registered"); }
  return find(id);
 }
}
