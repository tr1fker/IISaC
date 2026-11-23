package by.lab.analytics;
import by.lab.contracts.Models.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AnalyticsServiceTest {
 @Test void annualAndMonthlyResultsUseSuccessfulPaymentsAndRefundDates() {
  AnalyticsService service=new AnalyticsService(new AnalyticsRepository(null)) {
   @Override List<Payment> payments() { return List.of(
    new Payment("p1","u1","m1","pm1",new BigDecimal("125.50"),"BYN","SUCCESS","","2026-01-10T12:00:00Z"),
    new Payment("p2","u1","m1","pm1",new BigDecimal("45.00"),"BYN","CREATED","","2026-02-15T12:00:00Z")); }
   @Override List<Refund> refunds() { return List.of(new Refund("r1","p1",new BigDecimal("25.00"),"BYN","SUCCESS","2026-01-12T09:00:00Z")); }
  };
  var year=service.summary("m1","2026-01-01","2026-12-31");
  assertEquals(1,year.paymentCount()); assertEquals(0,new BigDecimal("100.50").compareTo(year.revenue()));
  var february=service.summary("m1","2026-02-01","2026-02-28");
  assertEquals(0,february.paymentCount()); assertEquals(0,BigDecimal.ZERO.compareTo(february.revenue()));
 }
}
