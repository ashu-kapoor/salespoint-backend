package com.ashutosh.customerservice.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories(basePackages = "com.ashutosh.customerservice.repository")
public class MongoConfiguration { // extends AbstractReactiveMongoConfiguration {

  /*@Bean
  public MongoClient mongoClient() {
      return MongoClients.create();
  }

  @Override
  protected String getDatabaseName() {
      return "reactive";
  }*/
}
