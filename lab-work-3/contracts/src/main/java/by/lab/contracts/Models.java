package by.lab.contracts;
import java.math.BigDecimal;
public final class Models {
 private Models() {}
 public record User(String id, String name, String email, String role) {}
 public record Merchant(String id, String userId, String name, String status) {}
 public record Payment(String id, String clientId, String merchantId, String paymentMethodId, BigDecimal amount, String currency, String status, String description, String createdAt) {}
 public record PaymentMethod(String id, String clientId, String type, String lastFour, String status) {}
 public record Account(String id, String ownerId, BigDecimal balance, String currency, String status) {}
 public record Refund(String id, String paymentId, BigDecimal amount, String currency, String status, String createdAt) {}
 public record Transaction(String id, String paymentId, String clientId, BigDecimal amount, String currency, String type, String createdAt) {}
 public record Notification(String id, String recipientId, String message, String status, String createdAt) {}
 public record Analytics(String id, String merchantId, String periodFrom, String periodTo, int paymentCount, BigDecimal totalAmount, BigDecimal refundAmount, BigDecimal revenue) {}
 public record PaymentFilter(String status, String dateFrom, String dateTo, BigDecimal minAmount, BigDecimal maxAmount) {}
 public record PaymentSort(String field, String direction) {}
 public record PaymentSearch(PaymentFilter filter, PaymentSort sort) {}
 public record TransactionFilter(String paymentId, String clientId) {}
 public record MethodFilter(String clientId) {}
 public record RefundFilter(String paymentId) {}
 public record DateRange(String from, String to) {}
 public record AnalyticsQuery(String merchantId, String from, String to) {}
}
