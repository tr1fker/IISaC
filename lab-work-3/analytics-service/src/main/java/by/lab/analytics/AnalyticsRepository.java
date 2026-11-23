package by.lab.analytics;
import by.lab.contracts.Models.Analytics;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class AnalyticsRepository {
 private final JdbcTemplate db;
 public AnalyticsRepository(JdbcTemplate db) { this.db=db; }
 public List<Analytics> findAll() { return db.query("select id,merchant_id,period_from,period_to,payment_count,total_amount,refund_amount,revenue from analytics.payment_statistics order by id",(rs,n) -> new Analytics(rs.getString("id"), rs.getString("merchant_id"), rs.getString("period_from"), rs.getString("period_to"), rs.getInt("payment_count"), rs.getBigDecimal("total_amount"), rs.getBigDecimal("refund_amount"), rs.getBigDecimal("revenue"))); }
 public Optional<Analytics> findById(String id) { return db.query("select id,merchant_id,period_from,period_to,payment_count,total_amount,refund_amount,revenue from analytics.payment_statistics where id=?",(rs,n) -> new Analytics(rs.getString("id"), rs.getString("merchant_id"), rs.getString("period_from"), rs.getString("period_to"), rs.getInt("payment_count"), rs.getBigDecimal("total_amount"), rs.getBigDecimal("refund_amount"), rs.getBigDecimal("revenue")),id).stream().findFirst(); }
}
