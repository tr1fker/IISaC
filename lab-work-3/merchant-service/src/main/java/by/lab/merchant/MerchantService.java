package by.lab.merchant;
import by.lab.contracts.Models.Merchant;
import java.util.*;
import org.springframework.stereotype.Service;
@Service public class MerchantService {
 private final MerchantRepository repository;
 public MerchantService(MerchantRepository repository) { this.repository=repository; }
 public List<Merchant> list() { return repository.findAll(); }
 public Merchant find(String id) { return repository.findById(id).orElse(null); }
}
