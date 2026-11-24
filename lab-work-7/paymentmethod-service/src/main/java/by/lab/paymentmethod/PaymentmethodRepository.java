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
 public void insert(String id,String clientId,String type,String lastFour) { db.update("insert into paymentmethod.payment_methods(id,client_id,type,last_four,status) values (?,?,?,?,'ACTIVE')",id,clientId,type,lastFour); }
 public int deactivate(String id) { return db.update("update paymentmethod.payment_methods set status='INACTIVE' where id=? and status='ACTIVE'",id); }
}
