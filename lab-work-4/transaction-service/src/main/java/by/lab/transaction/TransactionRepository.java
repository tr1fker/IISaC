package by.lab.transaction;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class TransactionRepository {
 private final JdbcTemplate db;
 public TransactionRepository(JdbcTemplate db) { this.db = db; }
 public List<Map<String,Object>> findAll() { return db.queryForList("select id,payment_id,client_id,amount,currency,type,created_at from transaction.transactions order by id"); }
 public Optional<Map<String,Object>> findById(String id) { return db.queryForList("select id,payment_id,client_id,amount,currency,type,created_at from transaction.transactions where id = ?", id).stream().findFirst(); }
 public void insert(String id,String payment,String client,java.math.BigDecimal amount,String currency,String type,String createdAt) { db.update("insert into transaction.transactions(id,payment_id,client_id,amount,currency,type,created_at) values (?,?,?,?,?,?,?) on conflict (id) do nothing",id,payment,client,amount,currency,type,createdAt); }
}
