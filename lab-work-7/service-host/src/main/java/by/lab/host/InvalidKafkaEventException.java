package by.lab.host;
/** Permanent event validation error: route to DLT rather than block the consumer. */
public class InvalidKafkaEventException extends RuntimeException {
 public InvalidKafkaEventException(String message,Throwable cause) { super(message,cause); }
}
