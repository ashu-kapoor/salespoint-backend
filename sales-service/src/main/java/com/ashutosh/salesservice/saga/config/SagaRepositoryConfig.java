package com.ashutosh.salesservice.saga.config;

import com.ashutosh.saga.framework.SagaRepository;
import com.ashutosh.salesservice.entity.SalesEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@Configuration
public class SagaRepositoryConfig {
  @Bean
  public SagaRepository<SalesEntity> sagaRepository(ReactiveMongoTemplate reactiveMongoTemplate) {
    return new SagaRepository<>(SalesEntity.class, reactiveMongoTemplate);
  }
}
