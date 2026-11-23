package by.lab.refund;
import java.util.*;
import by.lab.mock.MockBus;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Component;
@Component public class MockRefundParticipants implements RefundParticipants {
 public Map<String,Object> payment(String id) { return MockBus.call("payment","/records/"+id,null,new TypeReference<Map<String,Object>>() {}); }
}
