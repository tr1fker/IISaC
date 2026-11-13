package by.lab.analytics;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class AnalyticsRepository {
 private final JdbcTemplate db;
 public AnalyticsRepository(JdbcTemplate db) { this.db = db; }
 public List<Map<String,Object>> findAll() { return db.queryForList("select id,merchant_id,period_from,period_to,payment_count,total_amount,refund_amount,revenue from analytics.payment_statistics order by id"); }
 public Optional<Map<String,Object>> findById(String id) { return db.queryForList("select id,merchant_id,period_from,period_to,payment_count,total_amount,refund_amount,revenue from analytics.payment_statistics where id = ?", id).stream().findFirst(); }
}
