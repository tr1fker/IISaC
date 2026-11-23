package by.lab.account;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class AccountRepository {
 private final JdbcTemplate db;
 public AccountRepository(JdbcTemplate db) { this.db = db; }
 public List<Map<String,Object>> findAll() { return db.queryForList("select id,owner_id,balance,currency,status from account.accounts order by id"); }
 public Optional<Map<String,Object>> findById(String id) { return db.queryForList("select id,owner_id,balance,currency,status from account.accounts where id = ?", id).stream().findFirst(); }
 public int recordDebit(String paymentId,String owner,String currency,java.math.BigDecimal amount) { return db.update("insert into account.debits(payment_id,owner_id,currency,amount) values (?,?,?,?) on conflict(payment_id) do nothing",paymentId,owner,currency,amount); }
 public Map<String,Object> debitRecord(String paymentId) { return db.queryForMap("select owner_id,currency,amount from account.debits where payment_id=?",paymentId); }
 public int debit(String owner,String currency,java.math.BigDecimal amount) { return db.update("update account.accounts set balance=balance-? where owner_id=? and currency=? and status='ACTIVE' and balance>=?",amount,owner,currency,amount); }
}
