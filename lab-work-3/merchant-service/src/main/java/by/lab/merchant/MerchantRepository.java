package by.lab.merchant;
import by.lab.contracts.Models.Merchant;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
@Repository
public class MerchantRepository {
 private final JdbcTemplate db;
 public MerchantRepository(JdbcTemplate db) { this.db=db; }
 public List<Merchant> findAll() { return db.query("select id,user_id,name,status from merchant.merchants order by id",(rs,n) -> new Merchant(rs.getString("id"), rs.getString("user_id"), rs.getString("name"), rs.getString("status"))); }
 public Optional<Merchant> findById(String id) { return db.query("select id,user_id,name,status from merchant.merchants where id=?",(rs,n) -> new Merchant(rs.getString("id"), rs.getString("user_id"), rs.getString("name"), rs.getString("status")),id).stream().findFirst(); }
}
