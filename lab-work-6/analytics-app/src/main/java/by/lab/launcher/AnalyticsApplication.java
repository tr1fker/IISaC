package by.lab.launcher;
import by.lab.host.HostApplication;
import by.lab.analytics.AnalyticsService;
import by.lab.analytics.AnalyticsRepository;
import org.springframework.boot.*;
import java.util.Map;
public class AnalyticsApplication { public static void main(String[] args) { SpringApplication app=new SpringApplication(HostApplication.class,AnalyticsService.class,AnalyticsRepository.class); app.setDefaultProperties(Map.of("service.name","analytics","spring.kafka.consumer.group-id","analytics-service")); app.setWebApplicationType(WebApplicationType.NONE); app.run(args); } }
