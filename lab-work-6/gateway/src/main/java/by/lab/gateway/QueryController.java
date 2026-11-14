package by.lab.gateway;
import java.util.*;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;
@Controller
public class QueryController {
 private final ServiceClient client; private final AuthClient auth;
 public QueryController(ServiceClient client,AuthClient auth) { this.client=client; this.auth=auth; }
 private Map<String,Object> actor(DataFetchingEnvironment env) {
  String token=env.getGraphQlContext().get("authorization");
  if(token==null || !token.startsWith("Bearer ")) throw new SecurityFailure("UNAUTHORIZED","Authentication required");
  return auth.verify(token);
 }
 private boolean admin(Map<String,Object> a) { return "ADMIN".equals(a.get("role")); }
 private boolean role(Map<String,Object> a,String role) { return role.equals(a.get("role")); }
 private void forbid() { throw new SecurityFailure("FORBIDDEN","Access denied"); }
 private boolean ownsMerchant(Map<String,Object> a,String id) {
  Map<String,Object> m=client.find("merchant",id); return m!=null && Objects.equals(m.get("user_id"),a.get("id"));
 }
 private boolean canPayment(Map<String,Object> a,Map<String,Object> p) {
  return admin(a) || role(a,"CLIENT") && Objects.equals(a.get("id"),p.get("clientId")) || role(a,"MERCHANT") && ownsMerchant(a,Objects.toString(p.get("merchantId"),""));
 }
 private Map<String,Object> convert(Map<String,Object> row) {
  if(row==null) return null;
  Map<String,Object> result=new LinkedHashMap<>();
  row.forEach((key,value)-> { StringBuilder name=new StringBuilder(); boolean upper=false; for(char c:key.toCharArray()) { if(c=='_') upper=true; else { name.append(upper ? Character.toUpperCase(c) : c); upper=false; } } result.put(name.toString(),value); });
  return result;
 }
 private List<Map<String,Object>> all(String service) { return client.list(service).stream().map(this::convert).toList(); }
 private Map<String,Object> one(String service,String id) { return convert(client.find(service,id)); }
 private boolean matches(Map<String,Object> row,String key,Object value) { return value==null || Objects.toString(row.get(key),"").equals(value.toString()); }
 private Map<String,Object> scoped(Map<String,Object> a,Map<String,Object> filter) {
  Map<String,Object> f=new HashMap<>(filter==null?Map.of():filter);
  if(role(a,"CLIENT")) { if(f.get("clientId")!=null && !a.get("id").equals(f.get("clientId"))) forbid(); f.put("clientId",a.get("id")); }
  else if(role(a,"MERCHANT")) {
   String requested=Objects.toString(f.get("merchantId"),"");
   if(!requested.isEmpty() && !ownsMerchant(a,requested)) forbid();
   if(requested.isEmpty()) { List<Map<String,Object>> shops=all("merchant").stream().filter(m->a.get("id").equals(m.get("userId"))).toList(); if(shops.size()!=1) throw new SecurityFailure("FORBIDDEN","Specify an accessible merchant"); f.put("merchantId",shops.get(0).get("id")); }
  } else if(!admin(a)) forbid();
  return f;
 }
 private Map<String,Object> visiblePayment(Map<String,Object> a,String id) {
  Map<String,Object> p=one("payment",id); if(p!=null && !canPayment(a,p)) forbid(); return p;
 }
 @QueryMapping public Map<String,Object> user(@Argument String id,DataFetchingEnvironment env) { Map<String,Object> a=actor(env); if(!admin(a) && !id.equals(a.get("id"))) forbid(); return one("identity",id); }
 @QueryMapping public Map<String,Object> payment(@Argument String id,DataFetchingEnvironment env) { return visiblePayment(actor(env),id); }
 @QueryMapping public List<Map<String,Object>> payments(@Argument Map<String,Object> filter,@Argument Map<String,Object> sort,DataFetchingEnvironment env) { return client.searchPayments(scoped(actor(env),filter),sort).stream().map(this::convert).toList(); }
 @QueryMapping public Map<String,Object> paymentSummary(@Argument Map<String,Object> filter,DataFetchingEnvironment env) { return client.paymentSummary(scoped(actor(env),filter)); }
 @MutationMapping public Map<String,Object> createPayment(@Argument Map<String,Object> input,DataFetchingEnvironment env) { Map<String,Object> a=actor(env); if(!admin(a) && !(role(a,"CLIENT") && Objects.equals(a.get("id"),input.get("clientId")))) forbid(); return convert(client.createPayment(input)); }
 @MutationMapping public Map<String,Object> completePayment(@Argument String id,DataFetchingEnvironment env) { Map<String,Object> a=actor(env); Map<String,Object> p=visiblePayment(a,id); if(!admin(a) && !(role(a,"MERCHANT") && p!=null && ownsMerchant(a,p.get("merchantId").toString()))) forbid(); return convert(client.transitionPayment(id,"SUCCESS")); }
 @MutationMapping public Map<String,Object> cancelPayment(@Argument String id,DataFetchingEnvironment env) { Map<String,Object> a=actor(env); Map<String,Object> p=visiblePayment(a,id); if(!admin(a) && !(role(a,"CLIENT") && p!=null && a.get("id").equals(p.get("clientId")))) forbid(); return convert(client.transitionPayment(id,"CANCELLED")); }
 @QueryMapping public List<Map<String,Object>> transactions(@Argument String paymentId,@Argument String clientId,DataFetchingEnvironment env) {
  Map<String,Object> a=actor(env);
  return all("transaction").stream().filter(t -> matches(t,"paymentId",paymentId) && matches(t,"clientId",clientId)).filter(t -> { Map<String,Object> p=one("payment",t.get("paymentId").toString()); return p!=null && canPayment(a,p); }).toList();
 }
 @QueryMapping public List<Map<String,Object>> paymentMethods(@Argument String clientId,DataFetchingEnvironment env) { Map<String,Object> a=actor(env); if(!admin(a) && !(role(a,"CLIENT") && a.get("id").equals(clientId))) forbid(); return all("paymentmethod").stream().filter(p -> matches(p,"clientId",clientId)).toList(); }
 @QueryMapping public List<Map<String,Object>> refunds(@Argument String paymentId,DataFetchingEnvironment env) {
  Map<String,Object> a=actor(env); return all("refund").stream().filter(r -> matches(r,"paymentId",paymentId)).filter(r -> { Map<String,Object> p=one("payment",r.get("paymentId").toString()); return p!=null && canPayment(a,p); }).toList();
 }
 private List<Map<String,Object>> statistics(Map<String,Object> a,String merchantId,Map<String,Object> period) { if(!admin(a) && !(role(a,"MERCHANT") && ownsMerchant(a,merchantId))) forbid(); return all("analytics").stream().filter(x -> matches(x,"merchantId",merchantId) && x.get("periodFrom").toString().compareTo(period.get("to").toString())<=0 && x.get("periodTo").toString().compareTo(period.get("from").toString())>=0).toList(); }
 @QueryMapping public Map<String,Object> analytics(@Argument String merchantId,@Argument Map<String,Object> period,DataFetchingEnvironment env) { return statistics(actor(env),merchantId,period).stream().findFirst().orElse(null); }
 @QueryMapping public List<Map<String,Object>> reports(@Argument String merchantId,@Argument Map<String,Object> period,DataFetchingEnvironment env) { return statistics(actor(env),merchantId,period); }
 @SchemaMapping(typeName="Payment",field="client") public Map<String,Object> client(Map<String,Object> payment) { return one("identity",payment.get("clientId").toString()); }
 @SchemaMapping(typeName="Payment",field="merchant") public Map<String,Object> merchant(Map<String,Object> payment) { return one("merchant",payment.get("merchantId").toString()); }
 @SchemaMapping(typeName="Payment",field="paymentMethod") public Map<String,Object> paymentMethod(Map<String,Object> payment,DataFetchingEnvironment env) { Map<String,Object> a=actor(env); if(!admin(a) && !a.get("id").equals(payment.get("clientId"))) forbid(); return one("paymentmethod",payment.get("paymentMethodId").toString()); }
 @SchemaMapping(typeName="Payment",field="refunds") public List<Map<String,Object>> paymentRefunds(Map<String,Object> payment,DataFetchingEnvironment env) { return refunds(payment.get("id").toString(),env); }
 @SchemaMapping(typeName="Payment",field="transactions") public List<Map<String,Object>> paymentTransactions(Map<String,Object> payment,DataFetchingEnvironment env) { return transactions(payment.get("id").toString(),null,env); }
}

