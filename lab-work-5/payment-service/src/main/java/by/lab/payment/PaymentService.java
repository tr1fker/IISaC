package by.lab.payment;
import java.math.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.*;
import org.springframework.stereotype.Service;
@Service
public class PaymentService {
 private final PaymentRepository repository;
 private final PaymentParticipants participants;
 public PaymentService(PaymentRepository repository,PaymentParticipants participants) { this.repository=repository; this.participants=participants; }
 public List<Map<String,Object>> list() { return repository.findAll(); }
 public Map<String,Object> find(String id) { return repository.findById(id).orElse(null); }
 private static String required(Map<String,Object> input,String key) { Object value=input.get(key); if(value==null || value.toString().isBlank()) throw new IllegalArgumentException(key+" is required"); return value.toString().trim(); }
 private static BigDecimal amount(Object value) { try { BigDecimal n=new BigDecimal(value.toString()); if(n.signum()<=0 || n.scale()>2 || n.compareTo(new BigDecimal("9999999999999.99"))>0) throw new IllegalArgumentException("Amount must be positive, have at most two decimal places and fit numeric(15,2)"); return n; } catch (NullPointerException | NumberFormatException e) { throw new IllegalArgumentException("Invalid amount"); } }
 public Map<String,Object> create(Map<String,Object> input) {
  String client=required(input,"clientId"), merchant=required(input,"merchantId"), method=required(input,"paymentMethodId"), currency=required(input,"currency").toUpperCase(Locale.ROOT);
  BigDecimal sum=amount(input.get("amount"));
  if(!currency.matches("[A-Z]{3}")) throw new IllegalArgumentException("Currency must be a three-letter code");
  Map<String,Object> user=participants.user(client), shop=participants.merchant(merchant), card=participants.paymentMethod(method);
  if(user==null || !"CLIENT".equals(user.get("role"))) throw new IllegalArgumentException("Client does not exist");
  if(shop==null || !"ACTIVE".equals(shop.get("status"))) throw new IllegalArgumentException("Merchant does not exist or is inactive");
  if(card==null || !client.equals(card.get("client_id")) || !"ACTIVE".equals(card.get("status"))) throw new IllegalArgumentException("Payment method is inactive or does not belong to the client");
  String id=UUID.randomUUID().toString();
  repository.insert(id,client,merchant,method,sum,currency,(String)input.get("description"),Instant.now().toString()); return find(id);
 }
 public Map<String,Object> transition(String id,String status) {
  if(repository.transition(id,status)==0) { if(find(id)==null) throw new IllegalArgumentException("Payment not found"); throw new IllegalArgumentException("Only a CREATED payment can be "+status.toLowerCase(Locale.ROOT)); }
  return find(id);
 }
 private static Instant date(String value,boolean end) { try { if(value.length()==10) { LocalDate d=LocalDate.parse(value); return (end?d.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().minusNanos(1):d.atStartOfDay(ZoneOffset.UTC).toInstant()); } return Instant.parse(value); } catch(DateTimeParseException e) { throw new IllegalArgumentException("Invalid date; use YYYY-MM-DD or an ISO-8601 instant"); } }
 public List<Map<String,Object>> search(Map<String,Object> filter,Map<String,Object> sort) {
  Map<String,Object> f=filter==null?Map.of():filter;
  BigDecimal min=f.get("minAmount")==null?null:amount(f.get("minAmount")), max=f.get("maxAmount")==null?null:amount(f.get("maxAmount"));
  Instant from=f.get("dateFrom")==null?null:date(f.get("dateFrom").toString(),false), to=f.get("dateTo")==null?null:date(f.get("dateTo").toString(),true);
  if(min!=null && max!=null && min.compareTo(max)>0 || from!=null && to!=null && from.isAfter(to)) throw new IllegalArgumentException("Invalid filter range");
  List<Map<String,Object>> result=repository.findAll().stream().filter(p -> (f.get("status")==null || f.get("status").equals(p.get("status"))) && (f.get("merchantId")==null || f.get("merchantId").equals(p.get("merchant_id"))) && (f.get("clientId")==null || f.get("clientId").equals(p.get("client_id"))) && (f.get("currency")==null || f.get("currency").toString().equalsIgnoreCase(p.get("currency").toString())) && (min==null || new BigDecimal(p.get("amount").toString()).compareTo(min)>=0) && (max==null || new BigDecimal(p.get("amount").toString()).compareTo(max)<=0) && (from==null || !Instant.parse(p.get("created_at").toString()).isBefore(from)) && (to==null || !Instant.parse(p.get("created_at").toString()).isAfter(to))).collect(Collectors.toCollection(ArrayList::new));
  Map<String,Object> s=sort==null?Map.of():sort;
  Comparator<Map<String,Object>> comparator="AMOUNT".equals(s.get("field")) ? Comparator.comparing(p -> new BigDecimal(p.get("amount").toString())) : Comparator.comparing(p -> Instant.parse(p.get("created_at").toString()));
  if("DESC".equals(s.get("direction"))) comparator=comparator.reversed(); result.sort(comparator.thenComparing(p -> p.get("id").toString())); return result;
 }
 public Map<String,Object> summary(Map<String,Object> filter) {
  List<Map<String,Object>> rows=search(filter,null); Set<String> currencies=rows.stream().map(p -> p.get("currency").toString()).collect(Collectors.toSet());
  if(currencies.size()>1) throw new IllegalArgumentException("Filter by currency to aggregate payments in different currencies");
  BigDecimal total=rows.stream().map(p -> new BigDecimal(p.get("amount").toString())).reduce(BigDecimal.ZERO,BigDecimal::add);
  return Map.of("count",rows.size(),"totalAmount",total,"currency",currencies.isEmpty()?Objects.toString(filter==null?null:filter.get("currency"),"").toUpperCase(Locale.ROOT):currencies.iterator().next());
 }
}
