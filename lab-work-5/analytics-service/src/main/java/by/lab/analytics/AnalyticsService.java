package by.lab.analytics;
import java.util.*;
import org.springframework.stereotype.Service;
@Service
public class AnalyticsService {
 private final AnalyticsRepository repository;
 public AnalyticsService(AnalyticsRepository repository) { this.repository = repository; }
 public List<Map<String,Object>> list() { return repository.findAll(); }
 public Map<String,Object> find(String id) { return repository.findById(id).orElse(null); }
 private List<Map<String,Object>> remote(String service) { return by.lab.mock.MockBus.call(service,"/records",null,new com.fasterxml.jackson.core.type.TypeReference<List<Map<String,Object>>>() {}); }
 private boolean inPeriod(Object value,java.time.LocalDate from,java.time.LocalDate to) { var day=java.time.LocalDate.parse(value.toString().substring(0,10)); return !day.isBefore(from) && !day.isAfter(to); }
 public Map<String,Object> summary(String merchantId,String start,String end) {
  var from=java.time.LocalDate.parse(start); var to=java.time.LocalDate.parse(end); if(from.isAfter(to)) throw new IllegalArgumentException("Invalid period");
  var success=remote("payment").stream().filter(p -> merchantId.equals(p.get("merchant_id")) && "SUCCESS".equals(p.get("status"))).toList();
  var current=success.stream().filter(p -> inPeriod(p.get("created_at"),from,to)).toList();
  var ids=success.stream().map(p -> p.get("id").toString()).collect(java.util.stream.Collectors.toSet());
  var total=current.stream().map(p -> new java.math.BigDecimal(p.get("amount").toString())).reduce(java.math.BigDecimal.ZERO,java.math.BigDecimal::add);
  var refunded=remote("refund").stream().filter(r -> ids.contains(r.get("payment_id")) && "SUCCESS".equals(r.get("status")) && inPeriod(r.get("created_at"),from,to)).map(r -> new java.math.BigDecimal(r.get("amount").toString())).reduce(java.math.BigDecimal.ZERO,java.math.BigDecimal::add);
  Map<String,Object> result=new LinkedHashMap<>(); result.put("id",merchantId+":"+from+":"+to); result.put("merchant_id",merchantId); result.put("period_from",from.toString()); result.put("period_to",to.toString()); result.put("payment_count",current.size()); result.put("total_amount",total); result.put("refund_amount",refunded); result.put("revenue",total.subtract(refunded)); return result;
 }
 public List<Map<String,Object>> reports(String merchantId,String start,String end) {
  var from=java.time.LocalDate.parse(start); var to=java.time.LocalDate.parse(end); if(from.isAfter(to)) throw new IllegalArgumentException("Invalid period");
  List<Map<String,Object>> result=new ArrayList<>(); for(var day=from; !day.isAfter(to);) { var last=day.withDayOfMonth(day.lengthOfMonth()); var endDay=last.isAfter(to)?to:last; result.add(summary(merchantId,day.toString(),endDay.toString())); day=endDay.plusDays(1); } return result;
 }
}
