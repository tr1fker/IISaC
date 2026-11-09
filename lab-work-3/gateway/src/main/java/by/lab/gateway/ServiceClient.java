package by.lab.gateway;
import java.util.*;
/** Boundary for future remote service clients. */
public interface ServiceClient {
 List<Map<String,Object>> list(String service);
 Map<String,Object> find(String service, String id);
}
