package by.lab.payment;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class PaymentRepository {
 private final JdbcTemplate db;
 public PaymentRepository(JdbcTemplate db) { this.db=db; }
 public List<Map<String,Object>> findAll() { return db.queryForList("select id,client_id,merchant_id,payment_method_id,amount,currency,status,description,created_at from payment.payments order by id"); }
 public Optional<Map<String,Object>> findById(String id) { return db.queryForList("select id,client_id,merchant_id,payment_method_id,amount,currency,status,description,created_at from payment.payments where id = ?",id).stream().findFirst(); }
 public void insert(String id,String client,String merchant,String method,java.math.BigDecimal amount,String currency,String description,String createdAt) { db.update("insert into payment.payments (id,client_id,merchant_id,payment_method_id,amount,currency,status,description,created_at) values (?,?,?,?,?,?,'CREATED',?,?)",id,client,merchant,method,amount,currency,description,createdAt); }
 public int transition(String id,String status) { return db.update("update payment.payments set status=? where id=? and status='CREATED'",status,id); }
}
