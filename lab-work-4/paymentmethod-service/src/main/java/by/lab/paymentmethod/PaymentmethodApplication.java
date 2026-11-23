package by.lab.paymentmethod;
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
public class PaymentmethodApplication {
 private final PaymentmethodService service;
 public PaymentmethodApplication(PaymentmethodService service) { this.service=service; }
 public static void main(String[] args) { SpringApplication.run(PaymentmethodApplication.class,args); }
 @GetMapping("/records") public List<Map<String,Object>> list() { return service.list(); }
 @GetMapping("/records/{id}") public Map<String,Object> find(@PathVariable String id) { return service.find(id); }
  @PostMapping("/add") public Map<String,Object> add(@RequestBody AddMethod body) { return service.add(body.clientId(),body.type(),body.lastFour()); }
 @PostMapping("/remove/{id}") public boolean remove(@PathVariable String id) { return service.remove(id); }
 record AddMethod(String clientId,String type,String lastFour) {}
 @PostMapping("/search") public List<Map<String,Object>> search(@RequestBody Map<String,Object> filter) { return service.search(filter); }
 @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST) public Map<String,String> badRequest(IllegalArgumentException e) { return Map.of("message",e.getMessage()); }
 private Map<String,Object> map(JsonNode value) { return MockBus.JSON.convertValue(value,new TypeReference<Map<String,Object>>() {}); }
 @Bean public MockServer mockServer() { return new MockServer("paymentmethod",(op,body) -> {
   if(op.equals("/records")) return list();
   if(op.startsWith("/records/")) return find(op.substring(9));
   if(op.equals("/search")) return search(map(body));
   if(op.equals("/add")) return add(MockBus.JSON.treeToValue(body,AddMethod.class));
   if(op.startsWith("/remove/")) return remove(op.substring(8));
   throw new IllegalArgumentException("Unknown mock operation: "+op);
  }); }
}
