package by.lab.paymentmethod;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class PaymentmethodRepository {
 private final JdbcTemplate db;
 public PaymentmethodRepository(JdbcTemplate db) { this.db = db; }
 public List<Map<String,Object>> findAll() { return db.queryForList("select id,client_id,type,last_four,status from paymentmethod.payment_methods order by id"); }
 public Optional<Map<String,Object>> findById(String id) { return db.queryForList("select id,client_id,type,last_four,status from paymentmethod.payment_methods where id = ?", id).stream().findFirst(); }
}
