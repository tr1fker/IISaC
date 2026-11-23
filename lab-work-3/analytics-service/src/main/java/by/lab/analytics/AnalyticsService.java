package by.lab.analytics;
import by.lab.contracts.Models.*;
import java.util.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.stream.Collectors;
import org.springframework.web.client.RestClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
@Service public class AnalyticsService {
 private final AnalyticsRepository repository;
 public AnalyticsService(AnalyticsRepository repository) { this.repository=repository; }
 public List<Analytics> list() { return repository.findAll(); }
 public Analytics find(String id) { return repository.findById(id).orElse(null); }
 private String url(String service) { return System.getenv().getOrDefault(service.toUpperCase(Locale.ROOT)+"_SERVICE_URL","http://"+service+"-service:8080")+"/records"; }
 List<Payment> payments() { return RestClient.create().get().uri(url("payment")).retrieve().body(new ParameterizedTypeReference<List<Payment>>() {}); }
 List<Refund> refunds() { return RestClient.create().get().uri(url("refund")).retrieve().body(new ParameterizedTypeReference<List<Refund>>() {}); }
 private boolean inPeriod(String value,LocalDate from,LocalDate to) { LocalDate date=LocalDate.parse(value.substring(0,10)); return !date.isBefore(from) && !date.isAfter(to); }
 public Analytics summary(String merchantId,String start,String end) {
  LocalDate from=LocalDate.parse(start),to=LocalDate.parse(end); if(from.isAfter(to)) throw new IllegalArgumentException("Invalid period");
  List<Payment> success=payments().stream().filter(p -> merchantId.equals(p.merchantId()) && "SUCCESS".equals(p.status())).toList();
  List<Payment> current=success.stream().filter(p -> inPeriod(p.createdAt(),from,to)).toList();
  Set<String> ids=success.stream().map(Payment::id).collect(Collectors.toSet());
  BigDecimal total=current.stream().map(Payment::amount).reduce(BigDecimal.ZERO,BigDecimal::add);
  BigDecimal refunded=refunds().stream().filter(r -> ids.contains(r.paymentId()) && "SUCCESS".equals(r.status()) && inPeriod(r.createdAt(),from,to)).map(Refund::amount).reduce(BigDecimal.ZERO,BigDecimal::add);
  return new Analytics(merchantId+":"+start+":"+end,merchantId,start,end,current.size(),total,refunded,total.subtract(refunded));
 }
 public List<Analytics> reports(String merchantId,String from,String to) { if(LocalDate.parse(from).isAfter(LocalDate.parse(to))) throw new IllegalArgumentException("Invalid period"); return repository.findAll().stream().filter(r -> merchantId.equals(r.merchantId()) && r.periodFrom().compareTo(to)<=0 && r.periodTo().compareTo(from)>=0).toList(); }
}
