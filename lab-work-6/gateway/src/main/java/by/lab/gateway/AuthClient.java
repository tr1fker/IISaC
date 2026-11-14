package by.lab.gateway;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
@Component
public class AuthClient {
 private final RestClient rest;
 public AuthClient(@Value("${AUTH_URL:http://localhost:8081}") String url) { rest=RestClient.create(url); }
 @SuppressWarnings("unchecked")
 public Map<String,Object> post(String path,Map<String,Object> input,String token) {
  try { var req=rest.post().uri(path); if(token!=null) req.header("Authorization",token); return req.body(input).retrieve().body(Map.class); }
  catch(HttpClientErrorException e) { throw new SecurityFailure(e.getStatusCode().value()==401?"UNAUTHORIZED":"BAD_REQUEST", e.getStatusCode().value()==401?"Invalid credentials or session":"Invalid request or email already registered"); }
  catch(RestClientException e) { throw new SecurityFailure("INTERNAL_ERROR","Authentication service is unavailable"); }
 }
 @SuppressWarnings("unchecked") public Map<String,Object> verify(String token) {
  try { return rest.get().uri("/verify").header("Authorization",token).retrieve().body(Map.class); }
  catch(HttpClientErrorException e) { throw new SecurityFailure("UNAUTHORIZED","Invalid token or inactive session"); }
  catch(RestClientException e) { throw new SecurityFailure("INTERNAL_ERROR","Authentication service is unavailable"); }
 }
}

