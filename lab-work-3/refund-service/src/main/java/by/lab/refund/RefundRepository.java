package by.lab.refund;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class RefundRepository {
 private final JdbcTemplate db;
 public RefundRepository(JdbcTemplate db) { this.db = db; }
 public List<Map<String,Object>> findAll() { return db.queryForList("select id,payment_id,amount,currency,status,created_at from refund.refunds order by id"); }
 public Optional<Map<String,Object>> findById(String id) { return db.queryForList("select id,payment_id,amount,currency,status,created_at from refund.refunds where id = ?", id).stream().findFirst(); }
}
