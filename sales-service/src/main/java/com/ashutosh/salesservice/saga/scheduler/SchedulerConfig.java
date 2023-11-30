package com.ashutosh.salesservice.saga.scheduler;

import com.mongodb.reactivestreams.client.MongoClient;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.mongo.reactivestreams.ReactiveStreamsMongoLockProvider;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableAsync
public class SchedulerConfig {

  @Bean
  public LockProvider lockProvider(MongoClient mongo, MongoProperties properties) {
    return new ReactiveStreamsMongoLockProvider(mongo.getDatabase(properties.getDatabase()));
  }
}
