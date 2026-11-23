package by.lab.analytics;
import by.lab.contracts.Models.*;
import java.util.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
@SpringBootApplication @RestController
public class AnalyticsApplication {
 private final AnalyticsService service;
 public AnalyticsApplication(AnalyticsService service) { this.service=service; }
 public static void main(String[] args) { SpringApplication.run(AnalyticsApplication.class,args); }
 @GetMapping("/records") public List<Analytics> list() { return service.list(); }
 @GetMapping("/records/{id}") public Analytics find(@PathVariable String id) { return service.find(id); }
  @PostMapping("/summary") public Analytics summary(@RequestBody AnalyticsQuery request) { return service.summary(request.merchantId(),request.from(),request.to()); }
 @PostMapping("/reports") public List<Analytics> reports(@RequestBody AnalyticsQuery request) { return service.reports(request.merchantId(),request.from(),request.to()); }
}
