package by.lab.merchant;
import by.lab.contracts.Models.*;
import java.util.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
@SpringBootApplication @RestController
public class MerchantApplication {
 private final MerchantService service;
 public MerchantApplication(MerchantService service) { this.service=service; }
 public static void main(String[] args) { SpringApplication.run(MerchantApplication.class,args); }
 @GetMapping("/records") public List<Merchant> list() { return service.list(); }
 @GetMapping("/records/{id}") public Merchant find(@PathVariable String id) { return service.find(id); }

}
