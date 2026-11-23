package by.lab.payment;
import java.math.BigDecimal;
import java.util.*;
import by.lab.mock.MockBus;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;
@Component public class MockPaymentParticipants implements PaymentParticipants {
 private static final TypeReference<Map<String,Object>> MAP=new TypeReference<>() {};
 private static final TypeReference<Object> VOID=new TypeReference<>() {};
 private Map<String,Object> get(String service,String id) { return MockBus.call(service,"/records/"+id,null,MAP); }
 public Map<String,Object> user(String id) { return get("identity",id); }
 public Map<String,Object> merchant(String id) { return get("merchant",id); }
 public Map<String,Object> paymentMethod(String id) { return get("paymentmethod",id); }
 public void debit(String paymentId,String owner,String currency,BigDecimal amount) { try { MockBus.call("account","/debit",Map.of("paymentId",paymentId,"owner",owner,"currency",currency,"amount",amount),VOID); } catch(IllegalArgumentException e) { throw new AccountRejectedException(e.getMessage()); } }
 public void record(Map<String,Object> payment,String type) { MockBus.call("transaction","/record/"+type,payment,VOID); }
 public void notifyPayment(Map<String,Object> payment,String status) { MockBus.call("notification","/notify/"+status,payment,VOID); }
}
