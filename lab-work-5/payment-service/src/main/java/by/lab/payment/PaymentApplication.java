package by.lab.payment;
import java.util.*;
import by.lab.mock.MockBus;
import by.lab.mock.MockServer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
@org.springframework.scheduling.annotation.EnableScheduling
@SpringBootApplication @RestController
public class PaymentApplication {
 private final PaymentService service;
 public PaymentApplication(PaymentService service) { this.service=service; }
 public static void main(String[] args) { SpringApplication.run(PaymentApplication.class,args); }
 @GetMapping("/records") public List<Map<String,Object>> list() { return service.list(); }
 @GetMapping("/records/{id}") public Map<String,Object> find(@PathVariable String id) { return service.find(id); }
  @PostMapping("/search") public List<Map<String,Object>> search(@RequestBody Search body) { return service.search(body.filter(),body.sort()); }
 @PostMapping("/summary") public Map<String,Object> summary(@RequestBody Map<String,Object> filter) { return service.summary(filter); }
 @PostMapping("/create") public Map<String,Object> create(@RequestBody Map<String,Object> body) { return service.create(body); }
 @PostMapping("/transition/{id}/{status}") public Map<String,Object> transition(@PathVariable String id,@PathVariable String status) { return service.transition(id,status); }
 record Search(Map<String,Object> filter,Map<String,Object> sort) {}
 @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST) public Map<String,String> badRequest(IllegalArgumentException e) { return Map.of("message",e.getMessage()); }
 private Map<String,Object> map(JsonNode value) { return MockBus.JSON.convertValue(value,new TypeReference<Map<String,Object>>() {}); }
 @Bean public MockServer mockServer() { return new MockServer("payment",(op,body) -> {
   if(op.equals("/records")) return list();
   if(op.startsWith("/records/")) return find(op.substring(9));
   if(op.equals("/search")) { Search x=MockBus.JSON.treeToValue(body,Search.class); return search(x); }
   if(op.equals("/summary")) return summary(map(body));
   if(op.equals("/create")) return create(map(body));
   if(op.startsWith("/transition/")) { String[] x=op.split("/"); return transition(x[2],x[3]); }
   throw new IllegalArgumentException("Unknown mock operation: "+op);
  }); }
}
