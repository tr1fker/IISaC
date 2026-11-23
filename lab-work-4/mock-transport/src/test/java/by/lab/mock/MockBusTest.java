package by.lab.mock;
import com.fasterxml.jackson.core.type.TypeReference;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
class MockBusTest {
 @TempDir Path directory;
 @Test void routesCallsAndPropagatesBusinessErrorsWithoutNetwork() {
  System.setProperty("mock.bus.dir",directory.toString());
  MockServer server=new MockServer("test",(operation,input) -> { if(operation.equals("/bad")) throw new IllegalArgumentException("invalid"); return Map.of("value",input.path("value").asInt()+1); });
  server.start();
  try {
   Map<String,Integer> result=MockBus.call("test","/increment",Map.of("value",41),new TypeReference<Map<String,Integer>>() {});
   assertEquals(42,result.get("value"));
   var error=assertThrows(IllegalArgumentException.class,() -> MockBus.call("test","/bad",null,new TypeReference<Object>() {}));
   assertEquals("invalid",error.getMessage());
  } finally { server.stop(); System.clearProperty("mock.bus.dir"); }
 }
}
