package by.lab.launcher;
import by.lab.host.HostApplication;
import by.lab.account.AccountService;
import by.lab.account.AccountRepository;
import org.springframework.boot.*;
import java.util.Map;
public class AccountApplication { public static void main(String[] args) { SpringApplication app=new SpringApplication(HostApplication.class,AccountService.class,AccountRepository.class); app.setDefaultProperties(Map.of("service.name","account","spring.kafka.consumer.group-id","account-service")); app.setWebApplicationType(WebApplicationType.NONE); app.run(args); } }
