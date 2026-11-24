package by.lab.launcher;
import by.lab.host.HostApplication;
import by.lab.paymentmethod.PaymentmethodService;
import by.lab.paymentmethod.PaymentmethodRepository;
import org.springframework.boot.*;
import java.util.Map;
public class PaymentmethodApplication { public static void main(String[] args) { SpringApplication app=new SpringApplication(HostApplication.class,PaymentmethodService.class,PaymentmethodRepository.class); app.setDefaultProperties(Map.of("service.name","paymentmethod","spring.kafka.consumer.group-id","paymentmethod-service")); app.setWebApplicationType(WebApplicationType.NONE); app.run(args); } }
