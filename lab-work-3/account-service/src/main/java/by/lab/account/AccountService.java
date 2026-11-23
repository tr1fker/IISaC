package by.lab.account;
import by.lab.contracts.Models.Account;
import java.util.*;
import org.springframework.stereotype.Service;
@Service public class AccountService {
 private final AccountRepository repository;
 public AccountService(AccountRepository repository) { this.repository=repository; }
 public List<Account> list() { return repository.findAll(); }
 public Account find(String id) { return repository.findById(id).orElse(null); }
}
