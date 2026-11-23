package by.lab.notification;
import by.lab.contracts.Models.Notification;
import java.util.*;
import org.springframework.stereotype.Service;
@Service public class NotificationService {
 private final NotificationRepository repository;
 public NotificationService(NotificationRepository repository) { this.repository=repository; }
 public List<Notification> list() { return repository.findAll(); }
 public Notification find(String id) { return repository.findById(id).orElse(null); }
}
