package by.lab.payment;
import by.lab.contracts.Models.*;
import java.util.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
@SpringBootApplication @RestController
public class PaymentApplication {
 private final PaymentService service;
 public PaymentApplication(PaymentService service) { this.service=service; }
 public static void main(String[] args) { SpringApplication.run(PaymentApplication.class,args); }
 @GetMapping("/records") public List<Payment> list() { return service.list(); }
 @GetMapping("/records/{id}") public Payment find(@PathVariable String id) { return service.find(id); }
  @PostMapping("/search") public List<Payment> search(@RequestBody PaymentSearch request) { return service.search(request.filter(),request.sort()); }
}
