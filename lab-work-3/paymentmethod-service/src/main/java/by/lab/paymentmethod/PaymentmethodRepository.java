package by.lab.paymentmethod;
import by.lab.contracts.Models.PaymentMethod;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class PaymentmethodRepository {
 private final JdbcTemplate db;
 public PaymentmethodRepository(JdbcTemplate db) { this.db=db; }
 public List<PaymentMethod> findAll() { return db.query("select id,client_id,type,last_four,status from paymentmethod.payment_methods order by id",(rs,n) -> new PaymentMethod(rs.getString("id"), rs.getString("client_id"), rs.getString("type"), rs.getString("last_four"), rs.getString("status"))); }
 public Optional<PaymentMethod> findById(String id) { return db.query("select id,client_id,type,last_four,status from paymentmethod.payment_methods where id=?",(rs,n) -> new PaymentMethod(rs.getString("id"), rs.getString("client_id"), rs.getString("type"), rs.getString("last_four"), rs.getString("status")),id).stream().findFirst(); }
}
