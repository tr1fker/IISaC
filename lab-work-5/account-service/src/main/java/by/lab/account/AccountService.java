package by.lab.account;
import java.util.*;
import org.springframework.stereotype.Service;
@Service
public class AccountService {
 private final AccountRepository repository;
 public AccountService(AccountRepository repository) { this.repository = repository; }
 public List<Map<String,Object>> list() { return repository.findAll(); }
 public Map<String,Object> find(String id) { return repository.findById(id).orElse(null); }
 @org.springframework.transaction.annotation.Transactional
 public void debit(String paymentId,String owner,String currency,java.math.BigDecimal amount) {
  if(amount==null || amount.signum()<=0) throw new IllegalArgumentException("Invalid amount");
  if(repository.recordDebit(paymentId,owner,currency,amount)==0) { var old=repository.debitRecord(paymentId); if(!owner.equals(old.get("owner_id")) || !currency.equals(old.get("currency")) || amount.compareTo(new java.math.BigDecimal(old.get("amount").toString()))!=0) throw new IllegalArgumentException("Payment debit does not match previous attempt"); return; }
  if(repository.debit(owner,currency,amount)!=1) throw new IllegalArgumentException("Account unavailable or insufficient balance");
 }

}
