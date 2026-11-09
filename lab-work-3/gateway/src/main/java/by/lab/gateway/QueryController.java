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
 @QueryMapping public List<Map<String,Object>> payments(@Argument Map<String,Object> filter,@Argument Map<String,Object> sort) {
  Stream<Map<String,Object>> stream=all("payment").stream();
  if(filter!=null) stream=stream.filter(p -> matches(p,"status",filter.get("status")) && (filter.get("minAmount")==null || ((Number)p.get("amount")).doubleValue()>=((Number)filter.get("minAmount")).doubleValue()) && (filter.get("maxAmount")==null || ((Number)p.get("amount")).doubleValue()<=((Number)filter.get("maxAmount")).doubleValue()) && (filter.get("dateFrom")==null || p.get("createdAt").toString().compareTo(filter.get("dateFrom").toString())>=0) && (filter.get("dateTo")==null || p.get("createdAt").toString().compareTo(filter.get("dateTo").toString())<=0));
  List<Map<String,Object>> result=new ArrayList<>(stream.toList());
  if(sort!=null) { String field=Objects.toString(sort.getOrDefault("field","CREATED_AT")); Comparator<Map<String,Object>> comparator=field.equals("AMOUNT") ? Comparator.comparingDouble(p -> ((Number)p.get("amount")).doubleValue()) : Comparator.comparing(p -> p.get("createdAt").toString()); if("DESC".equals(sort.get("direction"))) comparator=comparator.reversed(); result.sort(comparator); }
  return result;
 }
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
