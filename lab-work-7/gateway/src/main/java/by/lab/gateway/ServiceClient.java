package by.lab.gateway;
import java.util.*;
public interface ServiceClient {
 List<Map<String,Object>> list(String service);
 Map<String,Object> find(String service,String id);
 List<Map<String,Object>> searchPayments(Map<String,Object> filter,Map<String,Object> sort);
 Map<String,Object> paymentSummary(Map<String,Object> filter);
 Map<String,Object> createPayment(Map<String,Object> input);
 Map<String,Object> transitionPayment(String id,String status);
}
