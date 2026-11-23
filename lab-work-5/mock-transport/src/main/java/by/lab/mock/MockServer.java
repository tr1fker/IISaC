package by.lab.mock;
import com.fasterxml.jackson.databind.JsonNode;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import org.springframework.context.SmartLifecycle;
/** Each process handles only requests addressed to its own service. */
public class MockServer implements SmartLifecycle {
 @FunctionalInterface public interface Handler { Object handle(String operation,JsonNode input) throws Exception; }
 private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(MockServer.class);
 private final Path dir; private final Handler handler; private volatile boolean running; private ScheduledExecutorService worker;
 public MockServer(String name,Handler handler) { this.dir=MockBus.root().resolve(name); this.handler=handler; }
 public void start() { try { Files.createDirectories(dir); } catch(Exception e) { throw new IllegalStateException(e); } running=true; worker=Executors.newSingleThreadScheduledExecutor(); worker.scheduleWithFixedDelay(this::poll,0,30,TimeUnit.MILLISECONDS); }
 private void poll() { try(var files=Files.list(dir)) { files.filter(path -> path.toString().endsWith(".request")).sorted().forEach(this::process); } catch(Exception e) { log.error("Mock request processing failed",e); } }
 private void process(Path request) { String stem=request.getFileName().toString().replace(".request",""); Path response=dir.resolve(stem+".response"),tmp=dir.resolve(stem+".response.tmp");
  try { JsonNode data=MockBus.JSON.readTree(Files.readAllBytes(request)); Map<String,Object> out=new LinkedHashMap<>();
   try { if(data.path("expiresAt").asLong() < System.currentTimeMillis()) throw new IllegalStateException("Mock request expired"); out.put("result",handler.handle(data.path("operation").asText(),data.get("input"))); } catch(Exception e) { out.put("error",Objects.toString(e.getMessage(),"Service operation failed")); out.put("badRequest",e instanceof IllegalArgumentException); }
   Files.write(tmp,MockBus.JSON.writeValueAsBytes(out)); Files.move(tmp,response,StandardCopyOption.ATOMIC_MOVE); Files.deleteIfExists(request);
  } catch(Exception e) { log.error("Cannot process mock request {}",request,e); }
 }
 public void stop() { running=false; if(worker!=null) worker.shutdownNow(); }
 public boolean isRunning() { return running; }
}
