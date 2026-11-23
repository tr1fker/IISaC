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
 @MutationMapping public Map<String,Object> createRefund(@Argument Map<String,Object> input,DataFetchingEnvironment env) {
  Map<String,Object> a=actor(env); String id=Objects.toString(input.get("paymentId"),""); Map<String,Object> payment=visiblePayment(a,id);
  if(payment==null) throw new IllegalArgumentException("Payment not found");
  if(!admin(a) && !(role(a,"CLIENT") && a.get("id").equals(payment.get("clientId"))) && !(role(a,"MERCHANT") && ownsMerchant(a,payment.get("merchantId").toString()))) forbid();
  return convert(client.createRefund(id,new java.math.BigDecimal(input.get("amount").toString())));
 }
 @MutationMapping public Map<String,Object> addPaymentMethod(@Argument Map<String,Object> input,DataFetchingEnvironment env) {
  Map<String,Object> a=actor(env); String clientId=Objects.toString(input.get("clientId"),"");
  if(!admin(a) && !(role(a,"CLIENT") && a.get("id").equals(clientId))) forbid();
  return convert(client.addPaymentMethod(clientId,Objects.toString(input.get("type"),""),Objects.toString(input.get("lastFour"),"")));
 }
 @MutationMapping public boolean removePaymentMethod(@Argument String paymentMethodId,DataFetchingEnvironment env) {
  Map<String,Object> a=actor(env); Map<String,Object> method=client.find("paymentmethod",paymentMethodId);
  if(method==null) throw new IllegalArgumentException("Payment method not found");
  if(!admin(a) && !(role(a,"CLIENT") && a.get("id").equals(method.get("client_id")))) forbid();
  return client.removePaymentMethod(paymentMethodId);
 }
 @QueryMapping public List<Map<String,Object>> transactions(@Argument String paymentId,@Argument String clientId,DataFetchingEnvironment env) {
  Map<String,Object> a=actor(env);
  Map<String,Object> f=new HashMap<>(); f.put("paymentId",paymentId); f.put("clientId",clientId); return client.search("transaction",f).stream().map(this::convert).filter(t -> { Map<String,Object> p=one("payment",t.get("paymentId").toString()); return p!=null && canPayment(a,p); }).toList();
 }
 @QueryMapping public List<Map<String,Object>> paymentMethods(@Argument String clientId,DataFetchingEnvironment env) { Map<String,Object> a=actor(env); if(!admin(a) && !(role(a,"CLIENT") && a.get("id").equals(clientId))) forbid(); return client.search("paymentmethod",Map.of("clientId",clientId)).stream().map(this::convert).toList(); }
 @QueryMapping public List<Map<String,Object>> refunds(@Argument String paymentId,DataFetchingEnvironment env) {
  Map<String,Object> a=actor(env); Map<String,Object> f=new HashMap<>(); f.put("paymentId",paymentId); return client.search("refund",f).stream().map(this::convert).filter(r -> { Map<String,Object> p=one("payment",r.get("paymentId").toString()); return p!=null && canPayment(a,p); }).toList();
 }
 private void authorizeAnalytics(Map<String,Object> a,String merchantId) { if(!admin(a) && !(role(a,"MERCHANT") && ownsMerchant(a,merchantId))) forbid(); }
 @QueryMapping public Map<String,Object> analytics(@Argument String merchantId,@Argument Map<String,Object> period,DataFetchingEnvironment env) { authorizeAnalytics(actor(env),merchantId); return convert(client.analytics(merchantId,period)); }
 @QueryMapping public List<Map<String,Object>> reports(@Argument String merchantId,@Argument Map<String,Object> period,DataFetchingEnvironment env) { authorizeAnalytics(actor(env),merchantId); return client.reports(merchantId,period).stream().map(this::convert).toList(); }
 @SchemaMapping(typeName="Payment",field="client") public Map<String,Object> client(Map<String,Object> payment,DataFetchingEnvironment env) { Map<String,Object> a=actor(env); if(!admin(a) && !a.get("id").equals(payment.get("clientId"))) forbid(); return one("identity",payment.get("clientId").toString()); }
 @SchemaMapping(typeName="Payment",field="merchant") public Map<String,Object> merchant(Map<String,Object> payment) { return one("merchant",payment.get("merchantId").toString()); }
 @SchemaMapping(typeName="Payment",field="paymentMethod") public Map<String,Object> paymentMethod(Map<String,Object> payment,DataFetchingEnvironment env) { Map<String,Object> a=actor(env); if(!admin(a) && !a.get("id").equals(payment.get("clientId"))) forbid(); return one("paymentmethod",payment.get("paymentMethodId").toString()); }
 @SchemaMapping(typeName="Payment",field="refunds") public List<Map<String,Object>> paymentRefunds(Map<String,Object> payment,DataFetchingEnvironment env) { return refunds(payment.get("id").toString(),env); }
 @SchemaMapping(typeName="Payment",field="transactions") public List<Map<String,Object>> paymentTransactions(Map<String,Object> payment,DataFetchingEnvironment env) { return transactions(payment.get("id").toString(),null,env); }
}

