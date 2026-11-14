package by.lab.host;
import by.lab.contract.*;
import by.lab.payment.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.*;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.*;
import java.util.*;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
@Component
public class RpcServer {
 private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(RpcServer.class);
 private final ApplicationContext context; private final JdbcTemplate db; private final ObjectMapper json=new ObjectMapper(); private Server server;
 private final String name=System.getenv().getOrDefault("SERVICE_NAME","payment");
 public RpcServer(ApplicationContext context,JdbcTemplate db){this.context=context;this.db=db;}
 @PostConstruct public void start() throws Exception { server=ServerBuilder.forPort(9090).addService(new Data()).addService(new Methods()).addService(new Accounts()).addService(new Payments()).build().start(); }
 @PreDestroy public void stop(){if(server!=null) server.shutdown();}
 private Object service() throws Exception {String title=Character.toUpperCase(name.charAt(0))+name.substring(1);return context.getBean(Class.forName("by.lab."+name+"."+title+"Service"));}
 private void fail(StreamObserver<?> out,Exception e){ Throwable t=e instanceof java.lang.reflect.InvocationTargetException ? e.getCause():e; log.error("gRPC request failed service={}",name,t); Status s=t instanceof StatusRuntimeException ? ((StatusRuntimeException)t).getStatus() : t instanceof IllegalArgumentException?Status.INVALID_ARGUMENT:Status.INTERNAL; out.onError(s.withDescription(t instanceof IllegalArgumentException ? t.getMessage() : "Service operation failed").asRuntimeException()); }
 private class Data extends DataServiceGrpc.DataServiceImplBase {
  @SuppressWarnings("unchecked") public void call(DataRequest r,StreamObserver<DataResponse> out){try {log.info("gRPC request service={} operation={} id={}",name,r.getOperation(),r.getId());Object s=service(),result; Object input=r.getPayloadJson().isEmpty()?null:json.readValue(r.getPayloadJson(),Object.class); switch(r.getOperation()) {
   case "list": result=s.getClass().getMethod("list").invoke(s);break;
   case "find": result=s.getClass().getMethod("find",String.class).invoke(s,r.getId());break;
   case "search": {var m=(Map<String,Object>)input;result=((PaymentService)s).search((Map<String,Object>)m.get("filter"),(Map<String,Object>)m.get("sort"));break;}
   case "summary": result=((PaymentService)s).summary((Map<String,Object>)input);break;
   case "create": result=context.getBean(PaymentOperations.class).create((Map<String,Object>)input);break;
   case "transition": result=context.getBean(PaymentOperations.class).transition(r.getId(),(String)input);break;
   default: throw new IllegalArgumentException("Unknown operation"); }
   out.onNext(DataResponse.newBuilder().setPayloadJson(json.writeValueAsString(result)).build());out.onCompleted();}catch(Exception e){fail(out,e);}}
 }
 private class Methods extends PaymentMethodServiceGrpc.PaymentMethodServiceImplBase { public void validatePaymentMethod(ValidatePaymentMethodRequest r,StreamObserver<ValidatePaymentMethodResponse> out){try{if(!name.equals("paymentmethod")) throw new IllegalStateException("Wrong service"); var rows=db.queryForList("select client_id,status from paymentmethod.payment_methods where id=?",r.getPaymentMethodId());var row=rows.isEmpty()?Map.of():rows.get(0);String status=Objects.toString(row.get("status"),"NOT_FOUND");out.onNext(ValidatePaymentMethodResponse.newBuilder().setStatus(status).setValid(r.getClientId().equals(row.get("client_id")) && status.equals("ACTIVE")).build());out.onCompleted();}catch(Exception e){fail(out,e);}} }
 private class Accounts extends AccountServiceGrpc.AccountServiceImplBase { public void checkAccount(CheckAccountRequest r,StreamObserver<CheckAccountResponse> out){try{if(!name.equals("account")) throw new IllegalStateException("Wrong service");var rows=db.queryForList("select status,balance,currency from account.accounts where id=? or owner_id=?",r.getAccountId(),r.getAccountId());var row=rows.stream().filter(x->r.getCurrency().equals(x.get("currency"))).findFirst().orElse(Map.of());String status=Objects.toString(row.get("status"),"NOT_FOUND");boolean ok=status.equals("ACTIVE") && new java.math.BigDecimal(row.get("balance").toString()).compareTo(java.math.BigDecimal.valueOf(r.getAmount()))>=0;out.onNext(CheckAccountResponse.newBuilder().setAllowed(ok).setStatus(status).build());out.onCompleted();}catch(Exception e){fail(out,e);}} }
 private class Payments extends PaymentServiceGrpc.PaymentServiceImplBase { public void getPayment(GetPaymentRequest r,StreamObserver<GetPaymentResponse> out){try{if(!name.equals("payment")) throw new IllegalStateException("Wrong service");var row=((PaymentService)service()).find(r.getPaymentId());if(row==null){out.onError(Status.NOT_FOUND.asRuntimeException());return;}out.onNext(GetPaymentResponse.newBuilder().setPaymentId(r.getPaymentId()).setClientId(row.get("client_id").toString()).setMerchantId(row.get("merchant_id").toString()).setAmount(Double.parseDouble(row.get("amount").toString())).setCurrency(row.get("currency").toString()).setStatus(row.get("status").toString()).build());out.onCompleted();}catch(Exception e){fail(out,e);}} }
}
