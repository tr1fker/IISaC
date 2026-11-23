package by.lab.refund;
import by.lab.contracts.Models.Refund;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class RefundRepository {
 private final JdbcTemplate db;
 public RefundRepository(JdbcTemplate db) { this.db=db; }
 public List<Refund> findAll() { return db.query("select id,payment_id,amount,currency,status,created_at from refund.refunds order by id",(rs,n) -> new Refund(rs.getString("id"), rs.getString("payment_id"), rs.getBigDecimal("amount"), rs.getString("currency"), rs.getString("status"), rs.getString("created_at"))); }
 public Optional<Refund> findById(String id) { return db.query("select id,payment_id,amount,currency,status,created_at from refund.refunds where id=?",(rs,n) -> new Refund(rs.getString("id"), rs.getString("payment_id"), rs.getBigDecimal("amount"), rs.getString("currency"), rs.getString("status"), rs.getString("created_at")),id).stream().findFirst(); }
}
