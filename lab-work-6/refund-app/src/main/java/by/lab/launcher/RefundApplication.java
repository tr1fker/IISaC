package by.lab.launcher;
import by.lab.host.HostApplication;
import by.lab.refund.RefundService;
import by.lab.refund.RefundRepository;
import org.springframework.boot.*;
import java.util.Map;
public class RefundApplication { public static void main(String[] args) { SpringApplication app=new SpringApplication(HostApplication.class,RefundService.class,RefundRepository.class); app.setDefaultProperties(Map.of("service.name","refund","spring.kafka.consumer.group-id","refund-service")); app.setWebApplicationType(WebApplicationType.NONE); app.run(args); } }
