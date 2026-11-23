package by.lab.gateway;
import by.lab.contracts.Models.*;
import java.util.List;
public interface ServiceClient {
 User user(String id); Merchant merchant(String id); Payment payment(String id); PaymentMethod paymentMethod(String id);
 List<Payment> payments(PaymentFilter filter,PaymentSort sort);
 List<Transaction> transactions(String paymentId,String clientId);
 List<PaymentMethod> paymentMethods(String clientId);
 List<Refund> refunds(String paymentId);
 Analytics analytics(String merchantId,DateRange period);
 List<Analytics> reports(String merchantId,DateRange period);
}
