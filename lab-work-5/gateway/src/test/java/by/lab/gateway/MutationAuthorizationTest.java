package by.lab.gateway;
import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingEnvironmentImpl;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MutationAuthorizationTest {
 private static DataFetchingEnvironment env() { return DataFetchingEnvironmentImpl.newDataFetchingEnvironment().graphQLContext(GraphQLContext.newContext().of("authorization","Bearer test").build()).build(); }
 private static class FakeAuth extends AuthClient {
  final Map<String,Object> actor;
  FakeAuth(String id,String role) { super("http://localhost:1"); actor=Map.of("id",id,"role",role); }
  @Override public Map<String,Object> verify(String token) { return actor; }
 }
 private static class FakeServices implements ServiceClient {
  int writes;
  public List<Map<String,Object>> list(String service) { return List.of(); }
  public Map<String,Object> find(String service,String id) { return switch(service) {
   case "payment" -> Map.of("id",id,"client_id","owner","merchant_id","shop");
   case "paymentmethod" -> Map.of("id",id,"client_id","owner");
   case "merchant" -> Map.of("id",id,"user_id","merchant-owner");
   default -> null; }; }
  public Map<String,Object> createRefund(String id,BigDecimal amount) { writes++; return Map.of("id","refund"); }
  public Map<String,Object> addPaymentMethod(String clientId,String type,String digits) { writes++; return Map.of("id","method"); }
  public boolean removePaymentMethod(String id) { writes++; return true; }
  public Map<String,Object> analytics(String m,Map<String,Object> p) { return Map.of(); }
  public List<Map<String,Object>> reports(String m,Map<String,Object> p) { return List.of(); }
  public List<Map<String,Object>> search(String s,Map<String,Object> f) { return List.of(); }
  public List<Map<String,Object>> searchPayments(Map<String,Object> f,Map<String,Object> s) { return List.of(); }
  public Map<String,Object> paymentSummary(Map<String,Object> f) { return Map.of(); }
  public Map<String,Object> createPayment(Map<String,Object> i) { return Map.of(); }
  public Map<String,Object> transitionPayment(String id,String status) { return Map.of(); }
 }
 @Test void otherClientCannotChangeOwnersObjects() {
  var services=new FakeServices(); var controller=new QueryController(services,new FakeAuth("other","CLIENT"));
  assertEquals("FORBIDDEN",assertThrows(SecurityFailure.class,() -> controller.createRefund(Map.of("paymentId","p1","amount",10),env())).code);
  assertEquals("FORBIDDEN",assertThrows(SecurityFailure.class,() -> controller.removePaymentMethod("pm1",env())).code);
  assertEquals("FORBIDDEN",assertThrows(SecurityFailure.class,() -> controller.addPaymentMethod(Map.of("clientId","owner","type","CARD","lastFour","1234"),env())).code);
  assertEquals(0,services.writes);
 }
 @Test void ownerCanInitiateRefundAndManageMethods() {
  var services=new FakeServices(); var controller=new QueryController(services,new FakeAuth("owner","CLIENT"));
  assertNotNull(controller.createRefund(Map.of("paymentId","p1","amount",10),env()));
  assertNotNull(controller.addPaymentMethod(Map.of("clientId","owner","type","CARD","lastFour","1234"),env()));
  assertTrue(controller.removePaymentMethod("pm1",env())); assertEquals(3,services.writes);
 }
 @Test void merchantCannotReadNestedClientProfile() {
  var services=new FakeServices(); var controller=new QueryController(services,new FakeAuth("merchant-owner","MERCHANT"));
  assertEquals("FORBIDDEN",assertThrows(SecurityFailure.class,() -> controller.client(Map.of("clientId","owner"),env())).code);
 }
}
