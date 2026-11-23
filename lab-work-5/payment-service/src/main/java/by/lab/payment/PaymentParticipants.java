package by.lab.payment;
import java.util.Map;
public interface PaymentParticipants {
 void debit(String paymentId,String owner,String currency,java.math.BigDecimal amount);
 void record(Map<String,Object> payment,String type);
 void notifyPayment(Map<String,Object> payment,String status);
 Map<String,Object> user(String id);
 Map<String,Object> merchant(String id);
 Map<String,Object> paymentMethod(String id);
}
