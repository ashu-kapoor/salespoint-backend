package com.ashutosh.inventoryservice.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories(basePackages = "com.ashutosh.inventoryservice.repository")
public class MongoConfiguration {}
