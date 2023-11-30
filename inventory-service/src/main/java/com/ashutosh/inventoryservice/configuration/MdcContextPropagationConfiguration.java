package com.ashutosh.inventoryservice.configuration;

import static org.springframework.util.CollectionUtils.isEmpty;

import io.micrometer.context.ContextRegistry;
import io.micrometer.context.ContextSnapshotFactory;
import java.util.List;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.WebFilter;

/**
 * 1. Will register ThreadLocalAccessors into ContextRegistry for fields listed in application.yml
 * as property value <b>management.tracing.baggage.correlation.fields</b> 2. Enables Automatic
 * Context Propagation for all reactive methods
 *
 * @see <a href="https://github.com/micrometer-metrics/context-propagation">context-propagation</a>
 */
@Configuration
@ConditionalOnClass({ContextRegistry.class, ContextSnapshotFactory.class})
@ConditionalOnProperty(
    value = "management.tracing.baggage.correlation.fields",
    matchIfMissing = true)
public class MdcContextPropagationConfiguration {

  /* @Bean
      Filter correlationFilter(){
  return (request, response, chain)->{
      String name= request.getParameter("X-CorrelationId");
      if(name!=null){
          MDC.put("correlationId", name);
      }
      chain.doFilter(request, response);
  };
      }*/

  @Bean
  WebFilter webFilter() {
    return (exchange, chain) -> {
      ServerHttpRequest request = exchange.getRequest();
      String requestId = getRequestId(request.getHeaders());
      MDC.put("correlationId", requestId);
      return chain.filter(exchange);
    };
  }

  private String getRequestId(HttpHeaders headers) {
    List<String> requestIdHeaders = headers.get("X-CorrelationId");
    return requestIdHeaders == null || requestIdHeaders.isEmpty()
        ? UUID.randomUUID().toString()
        : requestIdHeaders.get(0);
  }

  public MdcContextPropagationConfiguration(
      @Value("${management.tracing.baggage.correlation.fields}") List<String> fields) {
    // Hooks.enableAutomaticContextPropagation();
    if (!isEmpty(fields)) {
      fields.forEach(
          claim ->
              ContextRegistry.getInstance()
                  .registerThreadLocalAccessor(
                      claim,
                      () -> MDC.get(claim),
                      value -> MDC.put(claim, value),
                      () -> MDC.remove(claim)));
      //        return;
    }
  }
}
