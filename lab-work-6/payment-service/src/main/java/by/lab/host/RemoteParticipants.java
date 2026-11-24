package by.lab.host;
import by.lab.payment.PaymentParticipants;
import by.lab.contract.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;
@Component
public class RemoteParticipants implements PaymentParticipants {
 private final ObjectMapper json=new ObjectMapper();
 private Map<String,Object> find(String service,String id) { ManagedChannel c=ManagedChannelBuilder.forAddress(service+"-service",9090).usePlaintext().build(); try {String result=DataServiceGrpc.newBlockingStub(c).withDeadlineAfter(4,TimeUnit.SECONDS).call(DataRequest.newBuilder().setOperation("find").setId(id).build()).getPayloadJson(); return json.readValue(result,Map.class);} catch(StatusRuntimeException e){throw new IllegalStateException(service+" service unavailable",e);} catch(Exception e){throw new IllegalStateException(e);} finally{c.shutdown();} }
 public Map<String,Object> user(String id){return find("identity",id);} public Map<String,Object> merchant(String id){return find("merchant",id);} public Map<String,Object> paymentMethod(String id){return find("paymentmethod",id);}
}
