package by.lab.host;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
@Component
public class Events {
 private final JdbcTemplate db;private final KafkaTemplate<String,String> kafka;private final TransactionTemplate tx;private final ObjectMapper json=new ObjectMapper();private final String name=System.getenv().getOrDefault("SERVICE_NAME","payment");
 public Events(JdbcTemplate db,KafkaTemplate<String,String> kafka,TransactionTemplate tx){this.db=db;this.kafka=kafka;this.tx=tx;}
 @Scheduled(fixedDelay=2000) public void publish(){if(!name.equals("payment"))return;for(var row:db.queryForList("select event_id,payment_id,payload from payment.outbox where published=false order by created_at limit 100")){try{kafka.send("payment-events",row.get("payment_id").toString(),row.get("payload").toString()).get(10,TimeUnit.SECONDS);db.update("update payment.outbox set published=true where event_id=?",row.get("event_id"));}catch(Exception e){System.err.println("Kafka publication will be retried: "+e.getMessage());break;}}}
 @KafkaListener(topics="payment-events") public void consume(String value){if(!Set.of("transaction","notification","analytics").contains(name))return;try{Map<String,Object> e=json.readValue(value,Map.class);tx.executeWithoutResult(s->{String id=e.get("eventId").toString(),type=e.get("eventType").toString();int inserted=db.update("insert into "+name+".processed_events(event_id) values (?) on conflict do nothing",id);if(inserted==0)return;
  if(name.equals("transaction") && type.equals("PaymentConfirmed")) db.update("insert into transaction.transactions(id,payment_id,client_id,amount,currency,type,created_at) values (?,?,?,?,?,'PAYMENT',?)",id,e.get("paymentId"),e.get("clientId"),e.get("amount"),e.get("currency"),e.get("timestamp"));
  if(name.equals("notification") && (type.equals("PaymentConfirmed")||type.equals("PaymentCancelled"))) db.update("insert into notification.notifications(id,recipient_id,message,status,created_at) values (?,?,?,'CREATED',?)",id,e.get("clientId"),type+": "+e.get("paymentId"),e.get("timestamp"));
  if(name.equals("analytics") && type.equals("PaymentConfirmed")){String day=e.get("timestamp").toString().substring(0,10);String key=e.get("merchantId")+":"+e.get("currency")+":"+day;db.update("insert into analytics.payment_statistics(id,merchant_id,period_from,period_to,payment_count,total_amount,refund_amount,revenue) values (?,?,?,?,1,?,0,?) on conflict(id) do update set payment_count=analytics.payment_statistics.payment_count+1,total_amount=analytics.payment_statistics.total_amount+excluded.total_amount,revenue=analytics.payment_statistics.revenue+excluded.revenue",key,e.get("merchantId"),day,day,e.get("amount"),e.get("amount"));}
 });}catch(Exception ex){throw new IllegalStateException("Failed to process Kafka event",ex);}}
}
