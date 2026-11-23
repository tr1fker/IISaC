package by.lab.notification;
import by.lab.contracts.Models.Notification;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class NotificationRepository {
 private final JdbcTemplate db;
 public NotificationRepository(JdbcTemplate db) { this.db=db; }
 public List<Notification> findAll() { return db.query("select id,recipient_id,message,status,created_at from notification.notifications order by id",(rs,n) -> new Notification(rs.getString("id"), rs.getString("recipient_id"), rs.getString("message"), rs.getString("status"), rs.getString("created_at"))); }
 public Optional<Notification> findById(String id) { return db.query("select id,recipient_id,message,status,created_at from notification.notifications where id=?",(rs,n) -> new Notification(rs.getString("id"), rs.getString("recipient_id"), rs.getString("message"), rs.getString("status"), rs.getString("created_at")),id).stream().findFirst(); }
}
