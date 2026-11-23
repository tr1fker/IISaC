package by.lab.transaction;
import by.lab.contracts.Models.Transaction;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class TransactionRepository {
 private final JdbcTemplate db;
 public TransactionRepository(JdbcTemplate db) { this.db=db; }
 public List<Transaction> findAll() { return db.query("select id,payment_id,client_id,amount,currency,type,created_at from transaction.transactions order by id",(rs,n) -> new Transaction(rs.getString("id"), rs.getString("payment_id"), rs.getString("client_id"), rs.getBigDecimal("amount"), rs.getString("currency"), rs.getString("type"), rs.getString("created_at"))); }
 public Optional<Transaction> findById(String id) { return db.query("select id,payment_id,client_id,amount,currency,type,created_at from transaction.transactions where id=?",(rs,n) -> new Transaction(rs.getString("id"), rs.getString("payment_id"), rs.getString("client_id"), rs.getBigDecimal("amount"), rs.getString("currency"), rs.getString("type"), rs.getString("created_at")),id).stream().findFirst(); }
}
