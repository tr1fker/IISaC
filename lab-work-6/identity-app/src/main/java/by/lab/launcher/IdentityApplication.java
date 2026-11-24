package by.lab.launcher;
import by.lab.host.HostApplication;
import by.lab.identity.IdentityService;
import by.lab.identity.IdentityRepository;
import org.springframework.boot.*;
import java.util.Map;
public class IdentityApplication { public static void main(String[] args) { SpringApplication app=new SpringApplication(HostApplication.class,IdentityService.class,IdentityRepository.class); app.setDefaultProperties(Map.of("service.name","identity","spring.kafka.consumer.group-id","identity-service")); app.setWebApplicationType(WebApplicationType.NONE); app.run(args); } }
