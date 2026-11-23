package by.lab.payment;
import by.lab.contracts.Models.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;
@Service public class PaymentService {
 private final PaymentRepository repository;
 public PaymentService(PaymentRepository repository) { this.repository=repository; }
 public List<Payment> list() { return repository.findAll(); }
 public Payment find(String id) { return repository.findById(id).orElse(null); }
 public List<Payment> search(PaymentFilter f,PaymentSort sort) {
  LocalDate from=f==null || f.dateFrom()==null?null:LocalDate.parse(f.dateFrom());
  LocalDate to=f==null || f.dateTo()==null?null:LocalDate.parse(f.dateTo());
  if(from!=null && to!=null && from.isAfter(to) || f!=null && f.minAmount()!=null && f.maxAmount()!=null && f.minAmount().compareTo(f.maxAmount())>0) throw new IllegalArgumentException("Invalid filter range");
  List<Payment> result=new ArrayList<>(repository.findAll().stream().filter(row -> {
   LocalDate date=LocalDate.parse(row.createdAt().substring(0,10));
   return (f==null || f.status()==null || f.status().equals(row.status())) && (from==null || !date.isBefore(from)) && (to==null || !date.isAfter(to)) && (f==null || f.minAmount()==null || row.amount().compareTo(f.minAmount())>=0) && (f==null || f.maxAmount()==null || row.amount().compareTo(f.maxAmount())<=0);
  }).toList());
  Comparator<Payment> comparator=sort!=null && "AMOUNT".equals(sort.field()) ? Comparator.comparing(Payment::amount) : Comparator.comparing(Payment::createdAt);
  if(sort!=null && "DESC".equals(sort.direction())) comparator=comparator.reversed(); result.sort(comparator.thenComparing(Payment::id)); return result;
 }
}
