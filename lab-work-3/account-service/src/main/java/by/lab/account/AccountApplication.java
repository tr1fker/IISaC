package by.lab.account;
import by.lab.contracts.Models.*;
import java.util.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
@SpringBootApplication @RestController
public class AccountApplication {
 private final AccountService service;
 public AccountApplication(AccountService service) { this.service=service; }
 public static void main(String[] args) { SpringApplication.run(AccountApplication.class,args); }
 @GetMapping("/records") public List<Account> list() { return service.list(); }
 @GetMapping("/records/{id}") public Account find(@PathVariable String id) { return service.find(id); }

}
