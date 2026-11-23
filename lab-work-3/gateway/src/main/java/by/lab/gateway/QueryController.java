package by.lab.gateway;
import by.lab.contracts.Models.*;
import java.util.List;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;
@Controller public class QueryController {
 private final ServiceClient client;
 public QueryController(ServiceClient client) { this.client=client; }
 @QueryMapping public User user(@Argument String id) { return client.user(id); }
 @QueryMapping public Payment payment(@Argument String id) { return client.payment(id); }
 @QueryMapping public List<Payment> payments(@Argument PaymentFilter filter,@Argument PaymentSort sort) { return client.payments(filter,sort); }
 @QueryMapping public List<Transaction> transactions(@Argument String paymentId,@Argument String clientId) { return client.transactions(paymentId,clientId); }
 @QueryMapping public List<PaymentMethod> paymentMethods(@Argument String clientId) { return client.paymentMethods(clientId); }
 @QueryMapping public List<Refund> refunds(@Argument String paymentId) { return client.refunds(paymentId); }
 @QueryMapping public Analytics analytics(@Argument String merchantId,@Argument DateRange period) { return client.analytics(merchantId,period); }
 @QueryMapping public List<Analytics> reports(@Argument String merchantId,@Argument DateRange period) { return client.reports(merchantId,period); }
 @SchemaMapping(typeName="Payment",field="client") public User client(Payment payment) { return client.user(payment.clientId()); }
 @SchemaMapping(typeName="Payment",field="merchant") public Merchant merchant(Payment payment) { return client.merchant(payment.merchantId()); }
 @SchemaMapping(typeName="Payment",field="paymentMethod") public PaymentMethod paymentMethod(Payment payment) { return client.paymentMethod(payment.paymentMethodId()); }
 @SchemaMapping(typeName="Payment",field="refunds") public List<Refund> paymentRefunds(Payment payment) { return client.refunds(payment.id()); }
 @SchemaMapping(typeName="Payment",field="transactions") public List<Transaction> paymentTransactions(Payment payment) { return client.transactions(payment.id(),null); }
}
