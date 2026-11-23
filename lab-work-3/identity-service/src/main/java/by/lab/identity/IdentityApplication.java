package by.lab.identity;
import by.lab.contracts.Models.*;
import java.util.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
@SpringBootApplication @RestController
public class IdentityApplication {
 private final IdentityService service;
 public IdentityApplication(IdentityService service) { this.service=service; }
 public static void main(String[] args) { SpringApplication.run(IdentityApplication.class,args); }
 @GetMapping("/records") public List<User> list() { return service.list(); }
 @GetMapping("/records/{id}") public User find(@PathVariable String id) { return service.find(id); }

}
