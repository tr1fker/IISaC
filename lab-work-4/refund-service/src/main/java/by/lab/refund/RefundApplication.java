package by.lab.refund;
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
public class RefundApplication {
 private final RefundService service;
 public RefundApplication(RefundService service) { this.service=service; }
 public static void main(String[] args) { SpringApplication.run(RefundApplication.class,args); }
 @GetMapping("/records") public List<Map<String,Object>> list() { return service.list(); }
 @GetMapping("/records/{id}") public Map<String,Object> find(@PathVariable String id) { return service.find(id); }
  @PostMapping("/create") public Map<String,Object> create(@RequestBody CreateRefund body) { return service.create(body.paymentId(),body.amount()); }
 record CreateRefund(String paymentId,java.math.BigDecimal amount) {}
 @PostMapping("/search") public List<Map<String,Object>> search(@RequestBody Map<String,Object> filter) { return service.search(filter); }
 @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST) public Map<String,String> badRequest(IllegalArgumentException e) { return Map.of("message",e.getMessage()); }
 private Map<String,Object> map(JsonNode value) { return MockBus.JSON.convertValue(value,new TypeReference<Map<String,Object>>() {}); }
 @Bean public MockServer mockServer() { return new MockServer("refund",(op,body) -> {
   if(op.equals("/records")) return list();
   if(op.startsWith("/records/")) return find(op.substring(9));
   if(op.equals("/search")) return search(map(body));
   if(op.equals("/create")) return create(MockBus.JSON.treeToValue(body,CreateRefund.class));
   throw new IllegalArgumentException("Unknown mock operation: "+op);
  }); }
}
