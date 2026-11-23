package by.lab.payment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Component public class PaymentRecovery {
 private final PaymentWorkflow workflow; private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(PaymentRecovery.class);
 public PaymentRecovery(PaymentWorkflow workflow) { this.workflow=workflow; }
 @Scheduled(fixedDelayString="${PAYMENT_RECOVERY_DELAY:5000}") public void recover() { for(String id:workflow.pending()) try { workflow.execute(id); } catch(PaymentRejectedException e) { workflow.reject(id,e.getMessage()); } catch(Exception e) { log.warn("Payment {} remains pending for retry",id,e); } }
}
