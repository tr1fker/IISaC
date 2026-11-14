package by.lab.host;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.util.function.Supplier;
final class GrpcRetry {
 private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(GrpcRetry.class);
 static <T> T call(String service,String operation,Supplier<T> request) {
  for(int attempt=1;;attempt++) {
   try { log.info("gRPC service={} operation={} attempt={}",service,operation,attempt); return request.get(); }
   catch(StatusRuntimeException e) {
    var code=e.getStatus().getCode();
    if(attempt>=3 || !(code==Status.Code.UNAVAILABLE || code==Status.Code.DEADLINE_EXCEEDED || code==Status.Code.RESOURCE_EXHAUSTED)) { log.error("gRPC failed service={} operation={} code={}",service,operation,code); throw e; }
    log.warn("Retrying gRPC service={} operation={} code={} attempt={}",service,operation,code,attempt);
    try { Thread.sleep(200L*attempt); } catch(InterruptedException ex) { Thread.currentThread().interrupt(); throw Status.CANCELLED.withDescription("Request interrupted").withCause(ex).asRuntimeException(); }
   }
  }
 }
}
