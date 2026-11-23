package by.lab.gateway;
import by.lab.contracts.Models.*;
import java.util.*;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
@Component public class HttpServiceClient implements ServiceClient {
 private final RestClient http=RestClient.create();
 private String base(String service) { return System.getenv().getOrDefault(service.toUpperCase(Locale.ROOT)+"_SERVICE_URL","http://"+service+"-service:8080"); }
 private <T> T get(String service,String id,Class<T> type) { return http.get().uri(base(service)+"/records/{id}",id).retrieve().body(type); }
 public User user(String id) { return get("identity",id,User.class); }
 public Merchant merchant(String id) { return get("merchant",id,Merchant.class); }
 public Payment payment(String id) { return get("payment",id,Payment.class); }
 public PaymentMethod paymentMethod(String id) { return get("paymentmethod",id,PaymentMethod.class); }
 public List<Payment> payments(PaymentFilter filter,PaymentSort sort) { return http.post().uri(base("payment")+"/search").body(new PaymentSearch(filter,sort)).retrieve().body(new ParameterizedTypeReference<List<Payment>>() {}); }
 public List<Transaction> transactions(String paymentId,String clientId) { return http.post().uri(base("transaction")+"/search").body(new TransactionFilter(paymentId,clientId)).retrieve().body(new ParameterizedTypeReference<List<Transaction>>() {}); }
 public List<PaymentMethod> paymentMethods(String clientId) { return http.post().uri(base("paymentmethod")+"/search").body(new MethodFilter(clientId)).retrieve().body(new ParameterizedTypeReference<List<PaymentMethod>>() {}); }
 public List<Refund> refunds(String paymentId) { return http.post().uri(base("refund")+"/search").body(new RefundFilter(paymentId)).retrieve().body(new ParameterizedTypeReference<List<Refund>>() {}); }
 private AnalyticsQuery query(String merchantId,DateRange period) { return new AnalyticsQuery(merchantId,period.from(),period.to()); }
 public Analytics analytics(String merchantId,DateRange period) { return http.post().uri(base("analytics")+"/summary").body(query(merchantId,period)).retrieve().body(Analytics.class); }
 public List<Analytics> reports(String merchantId,DateRange period) { return http.post().uri(base("analytics")+"/reports").body(query(merchantId,period)).retrieve().body(new ParameterizedTypeReference<List<Analytics>>() {}); }
}
