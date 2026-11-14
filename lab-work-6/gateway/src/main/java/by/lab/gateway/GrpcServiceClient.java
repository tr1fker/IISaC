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
 private Object call(String s,String op,String id,Object payload) { try { var request=DataRequest.newBuilder().setOperation(op).setId(id==null?"":id).setPayloadJson(json.writeValueAsString(payload)).build(); String result=DataServiceGrpc.newBlockingStub(channels.get(s)).withDeadlineAfter(4,TimeUnit.SECONDS).call(request).getPayloadJson(); return json.readValue(result,Object.class); } catch(StatusRuntimeException e) { if(e.getStatus().getCode()==Status.Code.INVALID_ARGUMENT) throw new IllegalArgumentException(e.getStatus().getDescription()); throw new IllegalStateException("Service "+s+" unavailable: "+e.getStatus().getCode(),e); } catch(Exception e) { throw new IllegalStateException("Service communication failed",e); } }
 @SuppressWarnings("unchecked") public List<Map<String,Object>> list(String s){return (List<Map<String,Object>>)call(s,"list",null,null);}
 @SuppressWarnings("unchecked") public Map<String,Object> find(String s,String id){return (Map<String,Object>)call(s,"find",id,null);}
 @SuppressWarnings("unchecked") public List<Map<String,Object>> searchPayments(Map<String,Object> f,Map<String,Object> s){Map<String,Object> x=new HashMap<>();x.put("filter",f);x.put("sort",s);return (List<Map<String,Object>>)call("payment","search",null,x);}
 @SuppressWarnings("unchecked") public Map<String,Object> paymentSummary(Map<String,Object> f){return (Map<String,Object>)call("payment","summary",null,f);}
 @SuppressWarnings("unchecked") public Map<String,Object> createPayment(Map<String,Object> i){return (Map<String,Object>)call("payment","create",null,i);}
 @SuppressWarnings("unchecked") public Map<String,Object> transitionPayment(String id,String status){return (Map<String,Object>)call("payment","transition",id,status);}
 @PreDestroy public void close(){channels.values().forEach(ManagedChannel::shutdown);}
}