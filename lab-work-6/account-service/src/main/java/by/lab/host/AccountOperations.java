package by.lab.host;
import java.math.BigDecimal;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
@Component public class AccountOperations {
 private final JdbcTemplate db;
 public AccountOperations(JdbcTemplate db) { this.db=db; }
 @Transactional public void debit(String paymentId,String owner,String currency,BigDecimal amount) {
  if(amount.signum()<=0 || amount.scale()>2) throw new IllegalArgumentException("Invalid amount");
  int added=db.update("insert into account.debits(payment_id,owner_id,currency,amount) values (?,?,?,?) on conflict(payment_id) do nothing",paymentId,owner,currency,amount);
  if(added==0) { var old=db.queryForMap("select owner_id,currency,amount from account.debits where payment_id=?",paymentId); if(!owner.equals(old.get("owner_id")) || !currency.equals(old.get("currency")) || amount.compareTo(new BigDecimal(old.get("amount").toString()))!=0) throw new IllegalArgumentException("Conflicting debit"); return; }
  if(db.update("update account.accounts set balance=balance-? where owner_id=? and currency=? and status='ACTIVE' and balance>=?",amount,owner,currency,amount)!=1) throw new IllegalArgumentException("Account unavailable or insufficient balance");
 }
}
