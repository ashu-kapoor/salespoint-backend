package com.ashutosh.cdcservice.configuration;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;

// @Configuration
// @EnableReactiveMongoRepositories(basePackages = "com.ashutosh.cdcservice.repository")
@RequiredArgsConstructor
public class MongoConfiguration extends AbstractReactiveMongoConfiguration {

  private final MongoProperties mongoProperties;

  @Override
  public MongoClient reactiveMongoClient() {
    MongoCredential credential =
        MongoCredential.createCredential(
            mongoProperties.getUsername(),
            mongoProperties.getDatabase(),
            mongoProperties.getPassword());

    MongoClientSettings settings =
        MongoClientSettings.builder()
            .credential(credential)
            .retryWrites(Boolean.FALSE)
            // .applyToSocketSettings(builder ->
            // builder.readTimeout(mongoProperties.getSocketTimeout().intValue(),
            // TimeUnit.MILLISECONDS).connectTimeout(mongoProperties.getConnectTimeout().intValue(),
            // TimeUnit.MILLISECONDS))
            .applyToClusterSettings(
                builder ->
                    builder
                        .hosts(
                            Arrays.asList(
                                new ServerAddress(
                                    mongoProperties.getHost(), mongoProperties.getPort())))
                        .requiredReplicaSetName(mongoProperties.getReplicaSetName()))
            .build();

    return MongoClients.create(settings);
  }

  @Override
  protected String getDatabaseName() {
    return mongoProperties.getDatabase();
  }
}
