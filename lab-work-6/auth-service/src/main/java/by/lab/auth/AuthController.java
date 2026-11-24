package by.lab.auth;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import javax.crypto.SecretKey;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AuthController {
 private final JdbcTemplate db; private final StringRedisTemplate redis; private final SecretKey key; private final long ttl;
 private final BCryptPasswordEncoder passwords = new BCryptPasswordEncoder();
 public AuthController(JdbcTemplate db,StringRedisTemplate redis,@Value("${auth.jwt-secret}") String secret,@Value("${auth.session-seconds:3600}") long ttl) {
  this.db=db; this.redis=redis; this.key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.ttl=ttl;
  if(ttl<1) throw new IllegalArgumentException("Session TTL must be positive");
 }
 @PostConstruct public void init() {
  db.execute("CREATE SCHEMA IF NOT EXISTS auth");
  db.execute("CREATE TABLE IF NOT EXISTS auth.credentials (user_id varchar PRIMARY KEY, password_hash varchar NOT NULL)");
  seed("u1"); seed("u2"); seed("admin");
 }
 private void seed(String id) { db.update("INSERT INTO auth.credentials(user_id,password_hash) VALUES (?,?) ON CONFLICT DO NOTHING",id,passwords.encode("Lab12345!")); }
 @SuppressWarnings("unchecked") private Map<String,Object> identity(String operation,String id,Map<String,Object> payload,boolean internal) {
  io.grpc.ManagedChannel channel=io.grpc.ManagedChannelBuilder.forAddress(System.getenv().getOrDefault("IDENTITY_HOST","identity-service"),9090).usePlaintext().build();
  try { var request=by.lab.contract.DataRequest.newBuilder().setOperation(operation).setId(id==null?"":id).setPayloadJson(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload)).build(); var stub=by.lab.contract.DataServiceGrpc.newBlockingStub(channel).withDeadlineAfter(5,java.util.concurrent.TimeUnit.SECONDS); if(internal) { String secret=System.getenv("INTERNAL_SERVICE_SECRET"); if(secret==null || secret.isBlank()) throw new IllegalStateException("Internal authentication is not configured"); io.grpc.Metadata headers=new io.grpc.Metadata(); headers.put(io.grpc.Metadata.Key.of("x-internal-secret",io.grpc.Metadata.ASCII_STRING_MARSHALLER),secret); stub=stub.withInterceptors(io.grpc.stub.MetadataUtils.newAttachHeadersInterceptor(headers)); } return new com.fasterxml.jackson.databind.ObjectMapper().readValue(stub.call(request).getPayloadJson(),Map.class); }
  catch(io.grpc.StatusRuntimeException e) { if(e.getStatus().getCode()==io.grpc.Status.Code.INVALID_ARGUMENT) throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already registered"); throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Identity unavailable"); }
  catch(java.io.IOException e) { throw new IllegalStateException(e); } finally { channel.shutdown(); }
 }
 private static String value(Map<String,String> input,String field) { String v=input.get(field); if(v==null || v.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,field+" is required"); return v.trim(); }
 @PostMapping("/register") @Transactional public Map<String,Object> register(@RequestBody Map<String,String> input) {
  String name=value(input,"name"), email=value(input,"email").toLowerCase(Locale.ROOT), password=value(input,"password");
  if(!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || password.length()<8 || password.length()>72 || name.length()>100 || email.length()>254) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid name, email or password (8-72 characters required)");
  String id=UUID.nameUUIDFromBytes(("user:"+email).getBytes(StandardCharsets.UTF_8)).toString();
  try { db.update("INSERT INTO auth.credentials(user_id,password_hash) VALUES (?,?)",id,passwords.encode(password)); } catch(DuplicateKeyException e) { throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already registered"); }
  return identity("registerUser",null,Map.of("id",id,"name",name,"email",email),true);
 }
 @PostMapping("/login") public Map<String,Object> login(@RequestBody Map<String,String> input) {
  String email=value(input,"email").toLowerCase(Locale.ROOT), password=value(input,"password");
  Map<String,Object> u=identity("byEmail",email,null,false); String id=u==null?null:u.get("id").toString(); List<Map<String,Object>> rows=id==null?List.of():db.queryForList("SELECT password_hash FROM auth.credentials WHERE user_id=?",id);
  if(rows.isEmpty() || !passwords.matches(password,rows.get(0).get("password_hash").toString())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials");
  String sid=UUID.randomUUID().toString(); Date now=new Date(), expires=new Date(now.getTime()+ttl*1000);
  String token=Jwts.builder().subject(id).id(sid).issuedAt(now).expiration(expires).signWith(key).compact();
  redis.opsForValue().set("session:"+sid,id,Duration.ofSeconds(ttl));
  return Map.of("token",token,"expiresIn",ttl,"user",Map.of("id",id,"name",u.get("name"),"email",u.get("email"),"role",u.get("role")));
 }
 private Map<String,Object> verify(String token) {
  try {
   Claims claims=Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
   String id=claims.getSubject(), sid=claims.getId();
   if(id==null || sid==null || !id.equals(redis.opsForValue().get("session:"+sid))) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Session is absent or expired");
   Map<String,Object> user=identity("find",id,null,false); if(user==null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"User not found"); return user;
  } catch(JwtException | IllegalArgumentException e) { throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid or expired token"); }
 }
 private String bearer(String header) { if(header==null || !header.startsWith("Bearer ") || header.length()<=7) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Bearer token required"); return header.substring(7); }
 @GetMapping("/verify") public Map<String,Object> validate(@RequestHeader(value="Authorization",required=false) String header) { return verify(bearer(header)); }
 @PostMapping("/logout") public void logout(@RequestHeader(value="Authorization",required=false) String header) {
  String token=bearer(header); verify(token); Claims claims=Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(); redis.delete("session:"+claims.getId());
 }
}

