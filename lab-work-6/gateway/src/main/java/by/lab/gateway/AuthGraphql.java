package by.lab.gateway;
import java.util.*;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;
@Controller public class AuthGraphql {
 private final AuthClient auth;
 public AuthGraphql(AuthClient auth) { this.auth=auth; }
 @MutationMapping public Map<String,Object> register(@Argument String name,@Argument String email,@Argument String password) { return auth.post("/register",Map.of("name",name,"email",email,"password",password),null); }
 @MutationMapping public Map<String,Object> login(@Argument String email,@Argument String password) { return auth.post("/login",Map.of("email",email,"password",password),null); }
 @MutationMapping public boolean logout(DataFetchingEnvironment env) { String token=env.getGraphQlContext().get("authorization"); if(token==null) throw new SecurityFailure("UNAUTHORIZED","Authentication required"); auth.post("/logout",Map.of(),token); return true; }
}

