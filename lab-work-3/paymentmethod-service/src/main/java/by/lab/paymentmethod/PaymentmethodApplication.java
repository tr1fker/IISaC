package by.lab.paymentmethod;
import by.lab.contracts.Models.*;
import java.util.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
@SpringBootApplication @RestController
public class PaymentmethodApplication {
 private final PaymentmethodService service;
 public PaymentmethodApplication(PaymentmethodService service) { this.service=service; }
 public static void main(String[] args) { SpringApplication.run(PaymentmethodApplication.class,args); }
 @GetMapping("/records") public List<PaymentMethod> list() { return service.list(); }
 @GetMapping("/records/{id}") public PaymentMethod find(@PathVariable String id) { return service.find(id); }
  @PostMapping("/search") public List<PaymentMethod> search(@RequestBody MethodFilter request) { return service.search(request); }
}
