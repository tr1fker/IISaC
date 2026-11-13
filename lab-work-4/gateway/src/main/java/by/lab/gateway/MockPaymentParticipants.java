package by.lab.gateway;
import by.lab.payment.PaymentParticipants;
import by.lab.identity.IdentityService;
import by.lab.merchant.MerchantService;
import by.lab.paymentmethod.PaymentmethodService;
import java.util.Map;
import org.springframework.stereotype.Component;
@Component
public class MockPaymentParticipants implements PaymentParticipants {
 private final IdentityService identity; private final MerchantService merchant; private final PaymentmethodService methods;
 public MockPaymentParticipants(IdentityService identity,MerchantService merchant,PaymentmethodService methods) { this.identity=identity; this.merchant=merchant; this.methods=methods; }
 public Map<String,Object> user(String id) { return identity.find(id); }
 public Map<String,Object> merchant(String id) { return merchant.find(id); }
 public Map<String,Object> paymentMethod(String id) { return methods.find(id); }
}
