package by.lab.merchant;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class MerchantRepository {
 private final JdbcTemplate db;
 public MerchantRepository(JdbcTemplate db) { this.db = db; }
 public List<Map<String,Object>> findAll() { return db.queryForList("select id,user_id,name,status from merchant.merchants order by id"); }
 public Optional<Map<String,Object>> findById(String id) { return db.queryForList("select id,user_id,name,status from merchant.merchants where id = ?", id).stream().findFirst(); }
}
