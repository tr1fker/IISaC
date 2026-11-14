package by.lab.host;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.kafka.annotation.EnableKafka;
import java.util.*;
@SpringBootApplication @EnableScheduling @EnableKafka
public class HostApplication {
 public static void main(String[] args) throws Exception {
  String name=System.getenv().getOrDefault("SERVICE_NAME","payment");
  if(!List.of("identity","merchant","payment","paymentmethod","account","refund","transaction","notification","analytics").contains(name)) throw new IllegalArgumentException("Unknown service "+name);
  String title=Character.toUpperCase(name.charAt(0))+name.substring(1);
  SpringApplication app=new SpringApplication(HostApplication.class,Class.forName("by.lab."+name+"."+title+"Service"),Class.forName("by.lab."+name+"."+title+"Repository"));
  app.setWebApplicationType(WebApplicationType.NONE); app.run(args);
 }
}
