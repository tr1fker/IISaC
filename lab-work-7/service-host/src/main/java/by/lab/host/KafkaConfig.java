package by.lab.host;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.*;
import org.springframework.util.backoff.FixedBackOff;
@Configuration
public class KafkaConfig {
 private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(KafkaConfig.class);
 @Bean public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String,String> kafka){
  var recoverer=new DeadLetterPublishingRecoverer(kafka, (record,ex) -> new org.apache.kafka.common.TopicPartition(record.topic()+".DLT",record.partition()));
  recoverer.setFailIfSendResultIsError(true);
  return new DefaultErrorHandler((record,ex)->{ log.error("Kafka retries exhausted topic={} partition={} offset={} key={}",record.topic(),record.partition(),record.offset(),record.key(),ex); recoverer.accept(record,ex); },new FixedBackOff(3000L,3L));
 }
}
