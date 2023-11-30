package com.ashutosh.searchservice.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {
  /*@Value("spring.data.elasticsearch.client.reactive.endpoints")
  private String elasticHostAndPort;

  @Bean
  public TestClient testClient(ReactiveElasticsearchClient reactiveElasticsearchClient){
      return new TestClient();
  }*/
}
