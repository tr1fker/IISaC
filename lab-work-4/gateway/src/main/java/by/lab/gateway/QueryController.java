package by.lab.gateway;
import java.util.*;
import java.util.stream.*;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;
@Controller
public class QueryController {
 private final ServiceClient client;
 public QueryController(ServiceClient client) { this.client=client; }
 private Map<String,Object> convert(Map<String,Object> row) {
  if (row==null) return null;
  Map<String,Object> result=new LinkedHashMap<>();
  row.forEach((key,value)-> { StringBuilder name=new StringBuilder(); boolean upper=false; for(char c:key.toCharArray()) { if(c=='_') upper=true; else { name.append(upper ? Character.toUpperCase(c) : c); upper=false; } } result.put(name.toString(),value); });
  return result;
 }
 private List<Map<String,Object>> all(String service) { return client.list(service).stream().map(this::convert).toList(); }
 private Map<String,Object> one(String service,String id) { return convert(client.find(service,id)); }
 private boolean matches(Map<String,Object> row,String key,Object value) { return value==null || Objects.toString(row.get(key),"").equals(value.toString()); }
 @QueryMapping public Map<String,Object> user(@Argument String id) { return one("identity",id); }
 @QueryMapping public Map<String,Object> payment(@Argument String id) { return one("payment",id); }
 @QueryMapping public List<Map<String,Object>> payments(@Argument Map<String,Object> filter,@Argument Map<String,Object> sort) { return client.searchPayments(filter,sort).stream().map(this::convert).toList(); }
 @QueryMapping public Map<String,Object> paymentSummary(@Argument Map<String,Object> filter) { return client.paymentSummary(filter); }
 @MutationMapping public Map<String,Object> createPayment(@Argument Map<String,Object> input) { return convert(client.createPayment(input)); }
 @MutationMapping public Map<String,Object> completePayment(@Argument String id) { return convert(client.transitionPayment(id,"SUCCESS")); }
 @MutationMapping public Map<String,Object> cancelPayment(@Argument String id) { return convert(client.transitionPayment(id,"CANCELLED")); }
 @QueryMapping public List<Map<String,Object>> transactions(@Argument String paymentId,@Argument String clientId) { return all("transaction").stream().filter(t -> matches(t,"paymentId",paymentId) && matches(t,"clientId",clientId)).toList(); }
 @QueryMapping public List<Map<String,Object>> paymentMethods(@Argument String clientId) { return all("paymentmethod").stream().filter(p -> matches(p,"clientId",clientId)).toList(); }
 @QueryMapping public List<Map<String,Object>> refunds(@Argument String paymentId) { return all("refund").stream().filter(r -> matches(r,"paymentId",paymentId)).toList(); }
 private List<Map<String,Object>> statistics(String merchantId,Map<String,Object> period) { return all("analytics").stream().filter(a -> matches(a,"merchantId",merchantId) && a.get("periodFrom").toString().compareTo(period.get("to").toString())<=0 && a.get("periodTo").toString().compareTo(period.get("from").toString())>=0).toList(); }
 @QueryMapping public Map<String,Object> analytics(@Argument String merchantId,@Argument Map<String,Object> period) { return statistics(merchantId,period).stream().findFirst().orElse(null); }
 @QueryMapping public List<Map<String,Object>> reports(@Argument String merchantId,@Argument Map<String,Object> period) { return statistics(merchantId,period); }
 @SchemaMapping(typeName="Payment",field="client") public Map<String,Object> client(Map<String,Object> payment) { return one("identity",payment.get("clientId").toString()); }
 @SchemaMapping(typeName="Payment",field="merchant") public Map<String,Object> merchant(Map<String,Object> payment) { return one("merchant",payment.get("merchantId").toString()); }
 @SchemaMapping(typeName="Payment",field="paymentMethod") public Map<String,Object> paymentMethod(Map<String,Object> payment) { return one("paymentmethod",payment.get("paymentMethodId").toString()); }
 @SchemaMapping(typeName="Payment",field="refunds") public List<Map<String,Object>> paymentRefunds(Map<String,Object> payment) { return refunds(payment.get("id").toString()); }
 @SchemaMapping(typeName="Payment",field="transactions") public List<Map<String,Object>> paymentTransactions(Map<String,Object> payment) { return transactions(payment.get("id").toString(),null); }
}

