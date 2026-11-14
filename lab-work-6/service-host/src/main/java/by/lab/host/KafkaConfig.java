package by.lab.host;
import org.springframework.context.annotation.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
@Configuration
public class KafkaConfig {
 @Bean public DefaultErrorHandler kafkaErrorHandler(){ return new DefaultErrorHandler(new FixedBackOff(3000L,FixedBackOff.UNLIMITED_ATTEMPTS)); }
}
