package by.lab.launcher;
import by.lab.host.HostApplication;
import by.lab.notification.NotificationService;
import by.lab.notification.NotificationRepository;
import org.springframework.boot.*;
import java.util.Map;
public class NotificationApplication { public static void main(String[] args) { SpringApplication app=new SpringApplication(HostApplication.class,NotificationService.class,NotificationRepository.class); app.setDefaultProperties(Map.of("service.name","notification","spring.kafka.consumer.group-id","notification-service")); app.setWebApplicationType(WebApplicationType.NONE); app.run(args); } }
