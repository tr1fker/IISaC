package by.lab.merchant;
import java.util.*;
import org.springframework.stereotype.Service;
@Service
public class MerchantService {
 private final MerchantRepository repository;
 public MerchantService(MerchantRepository repository) { this.repository = repository; }
 public List<Map<String,Object>> list() { return repository.findAll(); }
 public Map<String,Object> find(String id) { return repository.findById(id).orElse(null); }
}
