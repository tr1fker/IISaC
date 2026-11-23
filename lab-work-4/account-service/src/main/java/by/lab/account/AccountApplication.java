package by.lab.account;
import java.util.*;
import by.lab.mock.MockBus;
import by.lab.mock.MockServer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
@SpringBootApplication @RestController
public class AccountApplication {
 private final AccountService service;
 public AccountApplication(AccountService service) { this.service=service; }
 public static void main(String[] args) { SpringApplication.run(AccountApplication.class,args); }
 @GetMapping("/records") public List<Map<String,Object>> list() { return service.list(); }
 @GetMapping("/records/{id}") public Map<String,Object> find(@PathVariable String id) { return service.find(id); }
  @PostMapping("/debit") public void debit(@RequestBody Debit body) { service.debit(body.paymentId(),body.owner(),body.currency(),body.amount()); }
 record Debit(String paymentId,String owner,String currency,java.math.BigDecimal amount) {}
 @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST) public Map<String,String> badRequest(IllegalArgumentException e) { return Map.of("message",e.getMessage()); }
 private Map<String,Object> map(JsonNode value) { return MockBus.JSON.convertValue(value,new TypeReference<Map<String,Object>>() {}); }
 @Bean public MockServer mockServer() { return new MockServer("account",(op,body) -> {
   if(op.equals("/records")) return list();
   if(op.startsWith("/records/")) return find(op.substring(9));
   if(op.equals("/debit")) { debit(MockBus.JSON.treeToValue(body,Debit.class)); return null; }
   throw new IllegalArgumentException("Unknown mock operation: "+op);
  }); }
}
