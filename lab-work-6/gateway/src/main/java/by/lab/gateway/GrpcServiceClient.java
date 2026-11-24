package by.lab.gateway;
import by.lab.contract.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;
import jakarta.annotation.PreDestroy;
@Component
public class GrpcServiceClient implements ServiceClient {
 private final ObjectMapper json; private final Map<String,ManagedChannel> channels=new HashMap<>();
 public GrpcServiceClient(ObjectMapper json) { this.json=json; for(String s:List.of("identity","merchant","payment","paymentmethod","account","refund","transaction","notification","analytics")) channels.put(s,ManagedChannelBuilder.forAddress(System.getenv().getOrDefault(s.toUpperCase()+"_HOST",s+"-service"),9090).usePlaintext().build()); }
 private Object call(String s,String op,String id,Object payload) { return call(s,op,id,payload,null); }
 private Object call(String s,String op,String id,Object payload,String token) { try { var request=DataRequest.newBuilder().setOperation(op).setId(id==null?"":id).setPayloadJson(json.writeValueAsString(payload)).build(); var stub=DataServiceGrpc.newBlockingStub(channels.get(s)).withDeadlineAfter(8,TimeUnit.SECONDS); if(token!=null) { Metadata h=new Metadata(); h.put(Metadata.Key.of("authorization",Metadata.ASCII_STRING_MARSHALLER),token); stub=stub.withInterceptors(io.grpc.stub.MetadataUtils.newAttachHeadersInterceptor(h)); } String result=stub.call(request).getPayloadJson(); return json.readValue(result,Object.class); } catch(StatusRuntimeException e) { if(e.getStatus().getCode()==Status.Code.INVALID_ARGUMENT) throw new IllegalArgumentException(e.getStatus().getDescription()); if(e.getStatus().getCode()==Status.Code.UNAUTHENTICATED) throw new SecurityFailure("UNAUTHORIZED","Authentication required"); if(e.getStatus().getCode()==Status.Code.PERMISSION_DENIED) throw new SecurityFailure("FORBIDDEN","Access denied"); throw new IllegalStateException("Service "+s+" unavailable: "+e.getStatus().getCode(),e); } catch(Exception e) { throw new IllegalStateException("Service communication failed",e); } }
 @SuppressWarnings("unchecked") public List<Map<String,Object>> list(String s){return (List<Map<String,Object>>)call(s,"list",null,null);}
 @SuppressWarnings("unchecked") public Map<String,Object> find(String s,String id){return (Map<String,Object>)call(s,"find",id,null);}
 @SuppressWarnings("unchecked") public List<Map<String,Object>> searchPayments(Map<String,Object> f,Map<String,Object> s){Map<String,Object> x=new HashMap<>();x.put("filter",f);x.put("sort",s);return (List<Map<String,Object>>)call("payment","search",null,x);}
 @SuppressWarnings("unchecked") public Map<String,Object> paymentSummary(Map<String,Object> f){return (Map<String,Object>)call("payment","summary",null,f);}
 @SuppressWarnings("unchecked") public Map<String,Object> createPayment(Map<String,Object> i,String token){return (Map<String,Object>)call("payment","create",null,i,token);}
 @SuppressWarnings("unchecked") public Map<String,Object> transitionPayment(String id,String status,String token){return (Map<String,Object>)call("payment","transition",id,status,token);}
 @SuppressWarnings("unchecked") public Map<String,Object> createRefund(String id,java.math.BigDecimal amount,String token){return (Map<String,Object>)call("refund","createRefund",null,Map.of("paymentId",id,"amount",amount),token);}
 @SuppressWarnings("unchecked") public Map<String,Object> addPaymentMethod(Map<String,Object> input,String token){return (Map<String,Object>)call("paymentmethod","addMethod",null,input,token);}
 public boolean removePaymentMethod(String id,String token){return Boolean.TRUE.equals(call("paymentmethod","removeMethod",id,null,token));}
 @PreDestroy public void close(){channels.values().forEach(ManagedChannel::shutdown);}
}