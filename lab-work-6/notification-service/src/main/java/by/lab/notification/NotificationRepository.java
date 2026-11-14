package by.lab.notification;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class NotificationRepository {
 private final JdbcTemplate db;
 public NotificationRepository(JdbcTemplate db) { this.db = db; }
 public List<Map<String,Object>> findAll() { return db.queryForList("select id,recipient_id,message,status,created_at from notification.notifications order by id"); }
 public Optional<Map<String,Object>> findById(String id) { return db.queryForList("select id,recipient_id,message,status,created_at from notification.notifications where id = ?", id).stream().findFirst(); }
}
