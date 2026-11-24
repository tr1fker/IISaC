package by.lab.launcher;
import by.lab.host.HostApplication;
import by.lab.transaction.TransactionService;
import by.lab.transaction.TransactionRepository;
import org.springframework.boot.*;
import java.util.Map;
public class TransactionApplication { public static void main(String[] args) { SpringApplication app=new SpringApplication(HostApplication.class,TransactionService.class,TransactionRepository.class); app.setDefaultProperties(Map.of("service.name","transaction","spring.kafka.consumer.group-id","transaction-service")); app.setWebApplicationType(WebApplicationType.NONE); app.run(args); } }
