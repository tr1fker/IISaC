package by.lab.gateway;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;
@Component
public class GraphqlErrors extends DataFetcherExceptionResolverAdapter {
 @Override protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
  if(ex instanceof SecurityFailure failure) return GraphqlErrorBuilder.newError(env).message(failure.getMessage()).extensions(java.util.Map.of("code",failure.code)).build();
  if(ex instanceof IllegalArgumentException) return GraphqlErrorBuilder.newError(env).message(ex.getMessage()).errorType(org.springframework.graphql.execution.ErrorType.BAD_REQUEST).build();
  if(ex instanceof IllegalStateException) return GraphqlErrorBuilder.newError(env).message(ex.getMessage()).extensions(java.util.Map.of("code","SERVICE_UNAVAILABLE")).build();
  return null;
 }
}
