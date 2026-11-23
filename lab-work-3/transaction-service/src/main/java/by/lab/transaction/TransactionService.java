package by.lab.transaction;
import by.lab.contracts.Models.*;
import java.util.*;
import org.springframework.stereotype.Service;
@Service public class TransactionService {
 private final TransactionRepository repository;
 public TransactionService(TransactionRepository repository) { this.repository=repository; }
 public List<Transaction> list() { return repository.findAll(); }
 public Transaction find(String id) { return repository.findById(id).orElse(null); }
 public List<Transaction> search(TransactionFilter f) { return repository.findAll().stream().filter(row -> (f.paymentId()==null || f.paymentId().equals(row.paymentId())) && (f.clientId()==null || f.clientId().equals(row.clientId()))).toList(); }
}
