package by.lab.identity;
import by.lab.contracts.Models.User;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class IdentityRepository {
 private final JdbcTemplate db;
 public IdentityRepository(JdbcTemplate db) { this.db=db; }
 public List<User> findAll() { return db.query("select id,name,email,role from identity.users order by id",(rs,n) -> new User(rs.getString("id"), rs.getString("name"), rs.getString("email"), rs.getString("role"))); }
 public Optional<User> findById(String id) { return db.query("select id,name,email,role from identity.users where id=?",(rs,n) -> new User(rs.getString("id"), rs.getString("name"), rs.getString("email"), rs.getString("role")),id).stream().findFirst(); }
}
