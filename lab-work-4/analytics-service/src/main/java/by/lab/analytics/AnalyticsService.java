package by.lab.analytics;
import java.util.*;
import org.springframework.stereotype.Service;
@Service
public class AnalyticsService {
 private final AnalyticsRepository repository;
 public AnalyticsService(AnalyticsRepository repository) { this.repository = repository; }
 public List<Map<String,Object>> list() { return repository.findAll(); }
 public Map<String,Object> find(String id) { return repository.findById(id).orElse(null); }
}
