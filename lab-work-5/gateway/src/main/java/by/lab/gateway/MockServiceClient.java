package by.lab.gateway;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;
import by.lab.mock.MockBus;
import com.fasterxml.jackson.core.type.TypeReference;

@Component public class MockServiceClient implements ServiceClient {
 private static final TypeReference<Map<String,Object>> MAP=new TypeReference<>() {};
 private static final TypeReference<List<Map<String,Object>>> LIST=new TypeReference<>() {};
 private <T> T post(String service,String path,Object body,TypeReference<T> type) { return MockBus.call(service,path,body,type); }
 public List<Map<String,Object>> list(String service) { return MockBus.call(service,"/records",null,LIST); }
 public Map<String,Object> find(String service,String id) { return MockBus.call(service,"/records/"+id,null,MAP); }
 public List<Map<String,Object>> search(String service,Map<String,Object> filter) { return post(service,"/search",filter,LIST); }
 public List<Map<String,Object>> searchPayments(Map<String,Object> filter,Map<String,Object> sort) { Map<String,Object> body=new HashMap<>(); body.put("filter",filter); body.put("sort",sort); return post("payment","/search",body,LIST); }
 public Map<String,Object> paymentSummary(Map<String,Object> filter) { return post("payment","/summary",filter==null?Map.of():filter,MAP); }
 public Map<String,Object> createPayment(Map<String,Object> input) { return post("payment","/create",input,MAP); }
 public Map<String,Object> transitionPayment(String id,String status) { return post("payment","/transition/"+id+"/"+status,Map.of(),MAP); }
 public Map<String,Object> createRefund(String paymentId,BigDecimal amount) { return post("refund","/create",Map.of("paymentId",paymentId,"amount",amount),MAP); }
 public Map<String,Object> addPaymentMethod(String clientId,String type,String lastFour) { Map<String,Object> user=find("identity",clientId); if(user==null || !"CLIENT".equals(user.get("role"))) throw new IllegalArgumentException("Client does not exist"); return post("paymentmethod","/add",Map.of("clientId",clientId,"type",type,"lastFour",lastFour),MAP); }
 public boolean removePaymentMethod(String id) { return Boolean.TRUE.equals(post("paymentmethod","/remove/"+id,Map.of(),new TypeReference<Boolean>() {})); }
 private Map<String,Object> period(String merchantId,Map<String,Object> value) { return Map.of("merchantId",merchantId,"from",value.get("from"),"to",value.get("to")); }
 public Map<String,Object> analytics(String merchantId,Map<String,Object> value) { return post("analytics","/summary",period(merchantId,value),MAP); }
 public List<Map<String,Object>> reports(String merchantId,Map<String,Object> value) { return post("analytics","/reports",period(merchantId,value),LIST); }
}
