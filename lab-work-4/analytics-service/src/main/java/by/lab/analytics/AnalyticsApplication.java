package by.lab.analytics;
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
public class AnalyticsApplication {
 private final AnalyticsService service;
 public AnalyticsApplication(AnalyticsService service) { this.service=service; }
 public static void main(String[] args) { SpringApplication.run(AnalyticsApplication.class,args); }
 @GetMapping("/records") public List<Map<String,Object>> list() { return service.list(); }
 @GetMapping("/records/{id}") public Map<String,Object> find(@PathVariable String id) { return service.find(id); }

 @PostMapping("/summary") public Map<String,Object> summary(@RequestBody Period body) { return service.summary(body.merchantId(),body.from(),body.to()); }
 @PostMapping("/reports") public List<Map<String,Object>> reports(@RequestBody Period body) { return service.reports(body.merchantId(),body.from(),body.to()); }
 record Period(String merchantId,String from,String to) {}
 @ExceptionHandler(IllegalArgumentException.class) @ResponseStatus(org.springframework.http.HttpStatus.BAD_REQUEST) public Map<String,String> badRequest(IllegalArgumentException e) { return Map.of("message",e.getMessage()); }
 private Map<String,Object> map(JsonNode value) { return MockBus.JSON.convertValue(value,new TypeReference<Map<String,Object>>() {}); }
 @Bean public MockServer mockServer() { return new MockServer("analytics",(op,body) -> {
   if(op.equals("/records")) return list();
   if(op.startsWith("/records/")) return find(op.substring(9));
   if(op.equals("/summary")) return summary(MockBus.JSON.treeToValue(body,Period.class));
   if(op.equals("/reports")) return reports(MockBus.JSON.treeToValue(body,Period.class));
   throw new IllegalArgumentException("Unknown mock operation: "+op);
  }); }
}
