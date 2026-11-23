package by.lab.auth;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import javax.crypto.SecretKey;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import by.lab.mock.MockBus;
import com.fasterxml.jackson.core.type.TypeReference;
@RestController public class AuthController {
 private final org.springframework.jdbc.core.JdbcTemplate db; private final StringRedisTemplate redis; private final SecretKey key; private final long ttl;
 private final BCryptPasswordEncoder passwords=new BCryptPasswordEncoder();
 private static final TypeReference<Map<String,Object>> MAP=new TypeReference<>() {};
 public AuthController(org.springframework.jdbc.core.JdbcTemplate db,StringRedisTemplate redis,@Value("${auth.jwt-secret}") String secret,@Value("${auth.session-seconds:3600}") long ttl) { this.db=db; this.redis=redis; this.key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.ttl=ttl; if(ttl<1) throw new IllegalArgumentException("Session TTL must be positive"); }
 @jakarta.annotation.PostConstruct public void init() {
  db.execute("CREATE SCHEMA IF NOT EXISTS auth"); db.execute("CREATE TABLE IF NOT EXISTS auth.credentials (user_id varchar PRIMARY KEY,password_hash varchar NOT NULL)");
  for(String id:List.of("u1","u2","admin")) db.update("INSERT INTO auth.credentials(user_id,password_hash) VALUES (?,?) ON CONFLICT DO NOTHING",id,passwords.encode("Lab12345!"));
 }
 private Map<String,Object> identity(String path,Object body) { try { return MockBus.call("identity",path,body,MAP); } catch(IllegalArgumentException e) { throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already registered"); } }
 private static String value(Map<String,String> input,String field) { String v=input.get(field); if(v==null || v.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,field+" is required"); return v.trim(); }
 @PostMapping("/register") @org.springframework.transaction.annotation.Transactional public Map<String,Object> register(@RequestBody Map<String,String> input) {
  String name=value(input,"name"),email=value(input,"email").toLowerCase(Locale.ROOT),password=value(input,"password");
  if(!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || password.length()<8 || password.length()>72 || name.length()>100 || email.length()>254) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid name, email or password");
  String id=UUID.nameUUIDFromBytes(("user:"+email).getBytes(StandardCharsets.UTF_8)).toString();
  try { db.update("insert into auth.credentials(user_id,password_hash) values (?,?)",id,passwords.encode(password)); }
  catch(org.springframework.dao.DuplicateKeyException e) { throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already registered"); }
  return identity("/internal/register",Map.of("id",id,"name",name,"email",email));
 }
 @PostMapping("/login") public Map<String,Object> login(@RequestBody Map<String,String> input) {
  String email=value(input,"email").toLowerCase(Locale.ROOT),password=value(input,"password");
  Map<String,Object> user=identity("/internal/by-email",Map.of("email",email));
  String id=user==null?null:user.get("id").toString();
  var rows=id==null?List.<Map<String,Object>>of():db.queryForList("select password_hash from auth.credentials where user_id=?",id);
  if(rows.isEmpty() || !passwords.matches(password,rows.get(0).get("password_hash").toString())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials");
  String sid=UUID.randomUUID().toString(); Date now=new Date(),expires=new Date(now.getTime()+ttl*1000); String token=Jwts.builder().subject(id).id(sid).issuedAt(now).expiration(expires).signWith(key).compact(); redis.opsForValue().set("session:"+sid,id,Duration.ofSeconds(ttl)); return Map.of("token",token,"expiresIn",ttl,"user",user);
 }
 private Map<String,Object> verify(String token) {
  try { Claims claims=Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(); String id=claims.getSubject(),sid=claims.getId(); if(id==null || sid==null || !id.equals(redis.opsForValue().get("session:"+sid))) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Session is absent or expired"); Map<String,Object> user=identity("/records/"+id,null); if(user==null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"User not found"); return user; }
  catch(JwtException | IllegalArgumentException e) { throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid or expired token"); }
 }
 private String bearer(String header) { if(header==null || !header.startsWith("Bearer ") || header.length()<=7) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Bearer token required"); return header.substring(7); }
 @GetMapping("/verify") public Map<String,Object> validate(@RequestHeader(value="Authorization",required=false) String header) { return verify(bearer(header)); }
 @PostMapping("/logout") public void logout(@RequestHeader(value="Authorization",required=false) String header) { String token=bearer(header); verify(token); Claims claims=Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(); redis.delete("session:"+claims.getId()); }
}
