package by.lab.launcher;
import by.lab.host.HostApplication;
import by.lab.payment.PaymentService;
import by.lab.payment.PaymentRepository;
import org.springframework.boot.*;
import java.util.Map;
public class PaymentApplication { public static void main(String[] args) { SpringApplication app=new SpringApplication(HostApplication.class,PaymentService.class,PaymentRepository.class); app.setDefaultProperties(Map.of("service.name","payment","spring.kafka.consumer.group-id","payment-service")); app.setWebApplicationType(WebApplicationType.NONE); app.run(args); } }
