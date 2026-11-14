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
 private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(GrpcServiceClient.class);
 private final org.springframework.data.redis.core.StringRedisTemplate redis;
 private final ObjectMapper json; private final Map<String,ManagedChannel> channels=new HashMap<>();
 public GrpcServiceClient(ObjectMapper json, org.springframework.data.redis.core.StringRedisTemplate redis) { this.json=json; this.redis=redis; for(String s:List.of("identity","merchant","payment","paymentmethod","account","refund","transaction","notification","analytics")) channels.put(s,ManagedChannelBuilder.forAddress(System.getenv().getOrDefault(s.toUpperCase()+"_HOST",s+"-service"),9090).usePlaintext().build()); }
 private Object call(String s,String op,String id,Object payload) {
  boolean cacheable=op.equals("find") && (s.equals("identity") || s.equals("merchant"));
  String key="reference:"+s+":"+id;
  if(cacheable) try { String cached=redis.opsForValue().get(key); if(cached!=null) { log.info("Cache hit service={} id={}",s,id); return json.readValue(cached,Object.class); } } catch(Exception e) { log.warn("Redis read failed for {}: {}",key,e.getMessage()); }
  boolean safe=Set.of("find","list","search","summary").contains(op);
  try {
   var request=DataRequest.newBuilder().setOperation(op).setId(id==null?"":id).setPayloadJson(json.writeValueAsString(payload)).build();
   String result=null;
   for(int attempt=1;attempt<=3;attempt++) {
    try { log.info("gRPC service={} operation={} id={} attempt={}",s,op,id,attempt); result=DataServiceGrpc.newBlockingStub(channels.get(s)).withDeadlineAfter(safe ? 4 : 45,TimeUnit.SECONDS).call(request).getPayloadJson(); break; }
    catch(StatusRuntimeException e) {
     var code=e.getStatus().getCode();
     if(code==Status.Code.INVALID_ARGUMENT) throw new IllegalArgumentException(e.getStatus().getDescription());
     if(!safe || attempt==3 || !(code==Status.Code.UNAVAILABLE || code==Status.Code.DEADLINE_EXCEEDED || code==Status.Code.RESOURCE_EXHAUSTED)) { log.error("gRPC failed service={} operation={} code={}",s,op,code); throw new IllegalStateException("Service "+s+" failed: "+code,e); }
     log.warn("Retrying gRPC service={} operation={} code={} attempt={}",s,op,code,attempt);
     try { Thread.sleep(200L*attempt); } catch(InterruptedException interrupted) { Thread.currentThread().interrupt(); throw new IllegalStateException("Request interrupted",interrupted); }
    }
   }
   Object value=json.readValue(result,Object.class);
   if(cacheable && value!=null) try { redis.opsForValue().set(key,result,java.time.Duration.ofSeconds(30)); log.info("Cache stored service={} id={}",s,id); } catch(Exception e) { log.warn("Redis write failed for {}: {}",key,e.getMessage()); }
   return value;
  } catch(IllegalArgumentException | IllegalStateException e) { throw e; } catch(Exception e) { throw new IllegalStateException("Service communication failed",e); }
 }
 @SuppressWarnings("unchecked") public List<Map<String,Object>> list(String s){return (List<Map<String,Object>>)call(s,"list",null,null);}
 @SuppressWarnings("unchecked") public Map<String,Object> find(String s,String id){return (Map<String,Object>)call(s,"find",id,null);}
 @SuppressWarnings("unchecked") public List<Map<String,Object>> searchPayments(Map<String,Object> f,Map<String,Object> s){Map<String,Object> x=new HashMap<>();x.put("filter",f);x.put("sort",s);return (List<Map<String,Object>>)call("payment","search",null,x);}
 @SuppressWarnings("unchecked") public Map<String,Object> paymentSummary(Map<String,Object> f){return (Map<String,Object>)call("payment","summary",null,f);}
 @SuppressWarnings("unchecked") public Map<String,Object> createPayment(Map<String,Object> i){return (Map<String,Object>)call("payment","create",null,i);}
 @SuppressWarnings("unchecked") public Map<String,Object> transitionPayment(String id,String status){return (Map<String,Object>)call("payment","transition",id,status);}
 @PreDestroy public void close(){channels.values().forEach(ManagedChannel::shutdown);}
}