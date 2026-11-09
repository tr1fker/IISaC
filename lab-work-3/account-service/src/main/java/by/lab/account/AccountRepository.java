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
}
