package by.lab.gateway;
import java.util.*;
import org.springframework.stereotype.Component;
import by.lab.identity.IdentityService;
import by.lab.merchant.MerchantService;
import by.lab.payment.PaymentService;
import by.lab.paymentmethod.PaymentmethodService;
import by.lab.account.AccountService;
import by.lab.refund.RefundService;
import by.lab.transaction.TransactionService;
import by.lab.notification.NotificationService;
import by.lab.analytics.AnalyticsService;
/** In-process mock transport: delegates to service layer without network calls. */
@Component
public class MockServiceClient implements ServiceClient {
 private final IdentityService identity;
 private final MerchantService merchant;
 private final PaymentService payment;
 private final PaymentmethodService paymentmethod;
 private final AccountService account;
 private final RefundService refund;
 private final TransactionService transaction;
 private final NotificationService notification;
 private final AnalyticsService analytics;
 public MockServiceClient(IdentityService identity, MerchantService merchant, PaymentService payment, PaymentmethodService paymentmethod, AccountService account, RefundService refund, TransactionService transaction, NotificationService notification, AnalyticsService analytics) { this.identity=identity; this.merchant=merchant; this.payment=payment; this.paymentmethod=paymentmethod; this.account=account; this.refund=refund; this.transaction=transaction; this.notification=notification; this.analytics=analytics; }
 public List<Map<String,Object>> list(String service) { return switch(service) { case "identity" -> identity.list(); case "merchant" -> merchant.list(); case "payment" -> payment.list(); case "paymentmethod" -> paymentmethod.list(); case "account" -> account.list(); case "refund" -> refund.list(); case "transaction" -> transaction.list(); case "notification" -> notification.list(); case "analytics" -> analytics.list(); default -> throw new IllegalArgumentException("Unknown service: " + service); }; }
 public Map<String,Object> find(String service,String id) { return switch(service) { case "identity" -> identity.find(id); case "merchant" -> merchant.find(id); case "payment" -> payment.find(id); case "paymentmethod" -> paymentmethod.find(id); case "account" -> account.find(id); case "refund" -> refund.find(id); case "transaction" -> transaction.find(id); case "notification" -> notification.find(id); case "analytics" -> analytics.find(id); default -> throw new IllegalArgumentException("Unknown service: " + service); }; }
}
