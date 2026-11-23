package by.lab.transaction;
import by.lab.contracts.Models.*;
import java.util.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
@SpringBootApplication @RestController
public class TransactionApplication {
 private final TransactionService service;
 public TransactionApplication(TransactionService service) { this.service=service; }
 public static void main(String[] args) { SpringApplication.run(TransactionApplication.class,args); }
 @GetMapping("/records") public List<Transaction> list() { return service.list(); }
 @GetMapping("/records/{id}") public Transaction find(@PathVariable String id) { return service.find(id); }
  @PostMapping("/search") public List<Transaction> search(@RequestBody TransactionFilter request) { return service.search(request); }
}
