package by.lab.payment;
import java.util.Map;
public interface PaymentParticipants {
 Map<String,Object> user(String id);
 Map<String,Object> merchant(String id);
 Map<String,Object> paymentMethod(String id);
}
