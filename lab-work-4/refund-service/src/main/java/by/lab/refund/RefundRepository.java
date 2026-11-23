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
 public void lockPayment(String id) { db.query("select pg_advisory_xact_lock(hashtext(?))",rs -> {},id); }
 public java.math.BigDecimal totalForPayment(String id) { return db.queryForObject("select coalesce(sum(amount),0) from refund.refunds where payment_id=? and status in ('CREATED','SUCCESS')",java.math.BigDecimal.class,id); }
 public void insert(String id,String paymentId,java.math.BigDecimal amount,String currency,String createdAt) { db.update("insert into refund.refunds(id,payment_id,amount,currency,status,created_at) values (?,?,?,?,'CREATED',?)",id,paymentId,amount,currency,createdAt); }
}
