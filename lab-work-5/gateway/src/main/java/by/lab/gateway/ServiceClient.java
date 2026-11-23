package by.lab.gateway;
import java.util.*;
public interface ServiceClient {
 List<Map<String,Object>> list(String service);
 Map<String,Object> find(String service,String id);
 Map<String,Object> analytics(String merchantId,Map<String,Object> period);
 List<Map<String,Object>> reports(String merchantId,Map<String,Object> period);
 List<Map<String,Object>> search(String service,Map<String,Object> filter);
 List<Map<String,Object>> searchPayments(Map<String,Object> filter,Map<String,Object> sort);
 Map<String,Object> paymentSummary(Map<String,Object> filter);
 Map<String,Object> createPayment(Map<String,Object> input);
 Map<String,Object> createRefund(String paymentId,java.math.BigDecimal amount);
 Map<String,Object> addPaymentMethod(String clientId,String type,String lastFour);
 boolean removePaymentMethod(String id);
 Map<String,Object> transitionPayment(String id,String status);
}
