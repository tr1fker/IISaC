package by.lab.notification;
import java.util.*;
import org.springframework.stereotype.Service;
@Service
public class NotificationService {
 private final NotificationRepository repository;
 public NotificationService(NotificationRepository repository) { this.repository = repository; }
 public List<Map<String,Object>> list() { return repository.findAll(); }
 public Map<String,Object> find(String id) { return repository.findById(id).orElse(null); }
 public void notifyPayment(Map<String,Object> payment,String status) { repository.insert(payment.get("id").toString()+":"+status,payment.get("client_id").toString(),"Payment "+payment.get("id")+" "+status,java.time.Instant.now().toString()); }
}
