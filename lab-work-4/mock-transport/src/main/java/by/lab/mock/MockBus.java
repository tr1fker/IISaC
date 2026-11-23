package by.lab.mock;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.core.type.TypeReference;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
/** Temporary local transport emulating service calls without gRPC or network access. */
public class MockBus {
 public static final ObjectMapper JSON=new ObjectMapper();
 public static Path root() { return Path.of(System.getProperty("mock.bus.dir",System.getenv().getOrDefault("MOCK_BUS_DIR","/mock-bus"))); }
 public static <T> T call(String service,String operation,Object input,TypeReference<T> type) {
  String id=UUID.randomUUID().toString(); Path dir=root().resolve(service),request=dir.resolve(id+".request"),answer=dir.resolve(id+".response"),temp=dir.resolve(id+".tmp");
  try {
   Files.createDirectories(dir); Map<String,Object> payload=new LinkedHashMap<>(); payload.put("operation",operation); payload.put("input",input); payload.put("expiresAt",System.currentTimeMillis()+30000);
   Files.write(temp,JSON.writeValueAsBytes(payload)); Files.move(temp,request,StandardCopyOption.ATOMIC_MOVE);
   long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(30);
   while(System.nanoTime()<deadline) { if(Files.exists(answer)) { var response=JSON.readTree(Files.readAllBytes(answer)); Files.deleteIfExists(answer); if(response.hasNonNull("error")) { String message=response.get("error").asText(); if(response.path("badRequest").asBoolean()) throw new IllegalArgumentException(message); throw new IllegalStateException(message); } return JSON.convertValue(response.get("result"),type); } Thread.sleep(25); }
   Files.deleteIfExists(request); throw new IllegalStateException("Mock service "+service+" did not respond");
  } catch(InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException("Interrupted waiting for mock service",e); }
    catch(java.io.IOException e) { throw new IllegalStateException("Mock service communication failed",e); }
 }
}
