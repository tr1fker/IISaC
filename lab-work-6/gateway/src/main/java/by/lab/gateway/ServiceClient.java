package by.lab.gateway;
import java.util.*;
public interface ServiceClient {
 List<Map<String,Object>> list(String service);
 Map<String,Object> find(String service,String id);
 List<Map<String,Object>> searchPayments(Map<String,Object> filter,Map<String,Object> sort);
 Map<String,Object> paymentSummary(Map<String,Object> filter);
 Map<String,Object> createPayment(Map<String,Object> input,String token);
 Map<String,Object> transitionPayment(String id,String status,String token);
 Map<String,Object> addPaymentMethod(Map<String,Object> input,String token);
 boolean removePaymentMethod(String id,String token);
 Map<String,Object> createRefund(String paymentId,java.math.BigDecimal amount,String token);
}
