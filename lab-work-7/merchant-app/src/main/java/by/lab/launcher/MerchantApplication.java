package by.lab.launcher;
import by.lab.host.HostApplication;
import by.lab.merchant.MerchantService;
import by.lab.merchant.MerchantRepository;
import org.springframework.boot.*;
import java.util.Map;
public class MerchantApplication { public static void main(String[] args) { SpringApplication app=new SpringApplication(HostApplication.class,MerchantService.class,MerchantRepository.class); app.setDefaultProperties(Map.of("service.name","merchant","spring.kafka.consumer.group-id","merchant-service")); app.setWebApplicationType(WebApplicationType.NONE); app.run(args); } }
