package by.lab.identity;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class IdentityRepository {
 private final JdbcTemplate db;
 public IdentityRepository(JdbcTemplate db) { this.db = db; }
 public List<Map<String,Object>> findAll() { return db.queryForList("select id,name,email,role from identity.users order by id"); }
 public Optional<Map<String,Object>> findById(String id) { return db.queryForList("select id,name,email,role from identity.users where id = ?", id).stream().findFirst(); }
}
