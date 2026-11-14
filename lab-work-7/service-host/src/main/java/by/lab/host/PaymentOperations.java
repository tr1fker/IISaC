package by.lab.host;
import by.lab.payment.PaymentService;
import by.lab.contract.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
@Component
public class PaymentOperations {
 private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(PaymentOperations.class);
 private final ApplicationContext context; private final JdbcTemplate db; private final ObjectMapper json=new ObjectMapper();
 public PaymentOperations(ApplicationContext c,JdbcTemplate db){context=c;this.db=db;}
 private PaymentService payment(){return context.getBean(PaymentService.class);}
 private void event(String type,Map<String,Object> p){try{String id=UUID.randomUUID().toString();Map<String,Object> e=new LinkedHashMap<>();e.put("eventId",id);e.put("eventType",type);e.put("paymentId",p.get("id"));e.put("clientId",p.get("client_id"));e.put("merchantId",p.get("merchant_id"));e.put("amount",p.get("amount"));e.put("currency",p.get("currency"));e.put("timestamp",Instant.now().toString());db.update("insert into payment.outbox(event_id,payment_id,payload) values (?,?,?)",id,p.get("id"),json.writeValueAsString(e));}catch(Exception ex){throw new IllegalStateException("Cannot create event",ex);}}
 @Transactional public Map<String,Object> create(Map<String,Object> input){Map<String,Object> p=payment().create(input);event("PaymentCreated",p);log.info("Payment created id={} client={} merchant={}",p.get("id"),p.get("client_id"),p.get("merchant_id"));return p;}
 @Transactional public Map<String,Object> transition(String id,String status){
  if(!Set.of("SUCCESS","CANCELLED").contains(status)) throw new IllegalArgumentException("Unsupported status");
  if(status.equals("SUCCESS")){Map<String,Object> p=payment().find(id);if(p==null) throw new IllegalArgumentException("Payment not found");if(!"CREATED".equals(p.get("status"))) throw new IllegalArgumentException("Only a CREATED payment can be confirmed");
   ManagedChannel methods=ManagedChannelBuilder.forAddress("paymentmethod-service",9090).usePlaintext().build(),accounts=ManagedChannelBuilder.forAddress("account-service",9090).usePlaintext().build();
   try{var m=GrpcRetry.call("paymentmethod","validate",()->PaymentMethodServiceGrpc.newBlockingStub(methods).withDeadlineAfter(4,TimeUnit.SECONDS).validatePaymentMethod(ValidatePaymentMethodRequest.newBuilder().setPaymentMethodId(p.get("payment_method_id").toString()).setClientId(p.get("client_id").toString()).build()));if(!m.getValid()) throw new IllegalArgumentException("Payment method is unavailable: "+m.getStatus());var a=GrpcRetry.call("account","check",()->AccountServiceGrpc.newBlockingStub(accounts).withDeadlineAfter(4,TimeUnit.SECONDS).checkAccount(CheckAccountRequest.newBuilder().setAccountId(p.get("client_id").toString()).setAmount(Double.parseDouble(p.get("amount").toString())).setCurrency(p.get("currency").toString()).build()));if(!a.getAllowed()) throw new IllegalArgumentException("Account cannot cover payment: "+a.getStatus());}catch(StatusRuntimeException e){throw e;}finally{methods.shutdown();accounts.shutdown();}
  }
  Map<String,Object> p=payment().transition(id,status);event(status.equals("SUCCESS")?"PaymentConfirmed":"PaymentCancelled",p);log.info("Payment transitioned id={} status={}",id,status);return p;
 }
}
