package by.lab.account;
import by.lab.contracts.Models.Account;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class AccountRepository {
 private final JdbcTemplate db;
 public AccountRepository(JdbcTemplate db) { this.db=db; }
 public List<Account> findAll() { return db.query("select id,owner_id,balance,currency,status from account.accounts order by id",(rs,n) -> new Account(rs.getString("id"), rs.getString("owner_id"), rs.getBigDecimal("balance"), rs.getString("currency"), rs.getString("status"))); }
 public Optional<Account> findById(String id) { return db.query("select id,owner_id,balance,currency,status from account.accounts where id=?",(rs,n) -> new Account(rs.getString("id"), rs.getString("owner_id"), rs.getBigDecimal("balance"), rs.getString("currency"), rs.getString("status")),id).stream().findFirst(); }
}
