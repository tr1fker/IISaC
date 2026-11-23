package by.lab.payment;
import by.lab.contracts.Models.Payment;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class PaymentRepository {
 private final JdbcTemplate db;
 public PaymentRepository(JdbcTemplate db) { this.db=db; }
 public List<Payment> findAll() { return db.query("select id,client_id,merchant_id,payment_method_id,amount,currency,status,description,created_at from payment.payments order by id",(rs,n) -> new Payment(rs.getString("id"), rs.getString("client_id"), rs.getString("merchant_id"), rs.getString("payment_method_id"), rs.getBigDecimal("amount"), rs.getString("currency"), rs.getString("status"), rs.getString("description"), rs.getString("created_at"))); }
 public Optional<Payment> findById(String id) { return db.query("select id,client_id,merchant_id,payment_method_id,amount,currency,status,description,created_at from payment.payments where id=?",(rs,n) -> new Payment(rs.getString("id"), rs.getString("client_id"), rs.getString("merchant_id"), rs.getString("payment_method_id"), rs.getBigDecimal("amount"), rs.getString("currency"), rs.getString("status"), rs.getString("description"), rs.getString("created_at")),id).stream().findFirst(); }
}
