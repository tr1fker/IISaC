package by.lab.notification;
import by.lab.contracts.Models.*;
import java.util.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
@SpringBootApplication @RestController
public class NotificationApplication {
 private final NotificationService service;
 public NotificationApplication(NotificationService service) { this.service=service; }
 public static void main(String[] args) { SpringApplication.run(NotificationApplication.class,args); }
 @GetMapping("/records") public List<Notification> list() { return service.list(); }
 @GetMapping("/records/{id}") public Notification find(@PathVariable String id) { return service.find(id); }

}
