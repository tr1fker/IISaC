package by.lab.payment;
import by.lab.contracts.Models.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PaymentServiceTest {
 @Test void dateBoundsIncludeEntireDayAndSortByAmount() {
  PaymentRepository repo=new PaymentRepository(null) { @Override public List<Payment> findAll() { return List.of(
   new Payment("p1","u1","m1","pm1",new BigDecimal("125.50"),"BYN","SUCCESS","","2026-01-10T12:00:00Z"),
   new Payment("p2","u1","m1","pm1",new BigDecimal("45.00"),"BYN","CREATED","","2026-01-10T23:59:59Z"),
   new Payment("p3","u1","m1","pm1",new BigDecimal("10.00"),"BYN","CREATED","","2026-01-11T00:00:00Z")); } };
  var rows=new PaymentService(repo).search(new PaymentFilter(null,"2026-01-10","2026-01-10",null,null),new PaymentSort("AMOUNT","ASC"));
  assertEquals(List.of("p2","p1"),rows.stream().map(Payment::id).toList());
 }
 @Test void rejectsInvertedDateRange() { assertThrows(IllegalArgumentException.class,() -> new PaymentService(new PaymentRepository(null)).search(new PaymentFilter(null,"2026-02-01","2026-01-01",null,null),null)); }
}
