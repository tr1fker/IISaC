package by.lab.gateway;
import org.springframework.graphql.server.*;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
@Component public class AuthInterceptor implements WebGraphQlInterceptor {
 private final AuthClient auth;
 public AuthInterceptor(AuthClient auth) { this.auth=auth; }
 public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request,Chain chain) {
  String header=request.getHeaders().getFirst("Authorization");
  request.configureExecutionInput((input,builder)->builder.graphQLContext(context -> {
   if(header!=null) context.put("authorization",header);
  }).build());
  return chain.next(request);
 }
}

