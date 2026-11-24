package by.lab.host;
import by.lab.payment.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
@Component public class PaymentOperations {
 private final PaymentService payment; private final PaymentWorkflow workflow; private final JdbcTemplate db; private final ObjectMapper json=new ObjectMapper();
 public PaymentOperations(PaymentService payment,PaymentWorkflow workflow,JdbcTemplate db) { this.payment=payment;this.workflow=workflow;this.db=db; }
 @Transactional public Map<String,Object> create(Map<String,Object> input) { Map<String,Object> p=payment.create(input); try { String id=UUID.randomUUID().toString(); Map<String,Object> e=new LinkedHashMap<>(); e.put("eventId",id); e.put("eventType","PaymentCreated"); e.put("paymentId",p.get("id")); e.put("clientId",p.get("client_id")); e.put("merchantId",p.get("merchant_id")); e.put("amount",p.get("amount")); e.put("currency",p.get("currency")); e.put("status",p.get("status")); e.put("timestamp",Instant.now().toString()); db.update("insert into payment.outbox(event_id,payment_id,payload) values (?,?,?)",id,p.get("id"),json.writeValueAsString(e)); } catch(Exception e) { throw new IllegalStateException("Cannot save payment event",e); } return p; }
 public Map<String,Object> transition(String id,String status) { workflow.start(id,status); try { return workflow.execute(id); } catch(PaymentRejectedException e) { workflow.reject(id,e.getMessage()); throw e; } }
}
