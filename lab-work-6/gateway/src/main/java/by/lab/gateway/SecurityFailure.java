package by.lab.gateway;
public class SecurityFailure extends RuntimeException {
 public final String code;
 public SecurityFailure(String code,String message) { super(message); this.code=code; }
}

