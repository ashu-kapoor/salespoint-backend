package com.ashutosh.cdcservice.configuration;

import java.io.IOException;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

// @Configuration
//
// @RequiredArgsConstructor
@Configuration
@EnableReactiveMongoRepositories(basePackages = "com.ashutosh.cdcservice.repository")
public class DynamicDBConfiguration {

  @Bean
  public BeanDefinitionRegistryPostProcessor beanPostProcessor(
      Environment env, ConfigurableEnvironment environment) throws IOException {

    return new CdcBeanDefinitionRegistryPostProcessor(environment);
  }
}
