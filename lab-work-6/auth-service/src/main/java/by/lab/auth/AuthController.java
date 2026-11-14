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
  db.execute("CREATE TABLE IF NOT EXISTS auth.credentials (user_id varchar PRIMARY KEY, email varchar NOT NULL UNIQUE, password_hash varchar NOT NULL)");
  db.update("INSERT INTO identity.users(id,name,email,role) VALUES ('admin','Administrator','admin@example.com','ADMIN') ON CONFLICT (id) DO NOTHING");
  seed("u1","anna@example.com"); seed("u2","pavel@example.com"); seed("admin","admin@example.com");
 }
 private void seed(String id,String email) { db.update("INSERT INTO auth.credentials(user_id,email,password_hash) VALUES (?,?,?) ON CONFLICT DO NOTHING",id,email,passwords.encode("Lab12345!")); }
 private static String value(Map<String,String> input,String field) { String v=input.get(field); if(v==null || v.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,field+" is required"); return v.trim(); }
 @PostMapping("/register") @Transactional public Map<String,Object> register(@RequestBody Map<String,String> input) {
  String name=value(input,"name"), email=value(input,"email").toLowerCase(Locale.ROOT), password=value(input,"password");
  if(!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$") || password.length()<8 || password.length()>72 || name.length()>100 || email.length()>254) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid name, email or password (8-72 characters required)");
  String id=UUID.randomUUID().toString();
  try { db.update("INSERT INTO auth.credentials VALUES (?,?,?)",id,email,passwords.encode(password)); db.update("INSERT INTO identity.users VALUES (?,?,?,?)",id,name,email,"CLIENT"); }
  catch(DuplicateKeyException e) { throw new ResponseStatusException(HttpStatus.CONFLICT,"Email already registered"); }
  return Map.of("id",id,"name",name,"email",email,"role","CLIENT");
 }
 @PostMapping("/login") public Map<String,Object> login(@RequestBody Map<String,String> input) {
  String email=value(input,"email").toLowerCase(Locale.ROOT), password=value(input,"password");
  List<Map<String,Object>> rows=db.queryForList("SELECT u.id,u.name,u.email,u.role,c.password_hash FROM auth.credentials c JOIN identity.users u ON u.id=c.user_id WHERE c.email=?",email);
  if(rows.isEmpty() || !passwords.matches(password,rows.get(0).get("password_hash").toString())) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials");
  Map<String,Object> u=rows.get(0); String id=u.get("id").toString(), sid=UUID.randomUUID().toString(); Date now=new Date(), expires=new Date(now.getTime()+ttl*1000);
  String token=Jwts.builder().subject(id).id(sid).issuedAt(now).expiration(expires).signWith(key).compact();
  redis.opsForValue().set("session:"+sid,id,Duration.ofSeconds(ttl));
  return Map.of("token",token,"expiresIn",ttl,"user",Map.of("id",id,"name",u.get("name"),"email",u.get("email"),"role",u.get("role")));
 }
 private Map<String,Object> verify(String token) {
  try {
   Claims claims=Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
   String id=claims.getSubject(), sid=claims.getId();
   if(id==null || sid==null || !id.equals(redis.opsForValue().get("session:"+sid))) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Session is absent or expired");
   List<Map<String,Object>> users=db.queryForList("SELECT id,name,email,role FROM identity.users WHERE id=?",id);
   if(users.isEmpty()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"User not found");
   return users.get(0);
  } catch(JwtException | IllegalArgumentException e) { throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid or expired token"); }
 }
 private String bearer(String header) { if(header==null || !header.startsWith("Bearer ") || header.length()<=7) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Bearer token required"); return header.substring(7); }
 @GetMapping("/verify") public Map<String,Object> validate(@RequestHeader(value="Authorization",required=false) String header) { return verify(bearer(header)); }
 @PostMapping("/logout") public void logout(@RequestHeader(value="Authorization",required=false) String header) {
  String token=bearer(header); verify(token); Claims claims=Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(); redis.delete("session:"+claims.getId());
 }
}

