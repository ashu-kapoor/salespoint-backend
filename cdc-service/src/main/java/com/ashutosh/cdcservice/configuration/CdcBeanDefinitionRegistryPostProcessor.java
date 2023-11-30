package com.ashutosh.cdcservice.configuration;

import com.ashutosh.cdcservice.Model;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

public class CdcBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

  private static final String PROPERTY_KEY = "spring.cdc";
  private DatabaseConfiguration databaseConfiguration;

  public CdcBeanDefinitionRegistryPostProcessor(ConfigurableEnvironment environment)
      throws IOException {

    Map<String, Map<String, Object>> keyMap = new HashMap<>();

    for (PropertySource<?> source : environment.getPropertySources()) {
      if (source instanceof OriginTrackedMapPropertySource) {
        EnumerablePropertySource<?> propertySource = (OriginTrackedMapPropertySource) source;
        this.databaseConfiguration = generateDatabaseConfiguration(propertySource, keyMap);
        // System.out.println("break point line");
      }
    }
    // throw new IllegalStateException("Unable to determine value of property " + PROPERTY_KEY);
    // TODO: check if non cdc bean created
  }

  private DatabaseConfiguration generateDatabaseConfiguration(
      EnumerablePropertySource<?> propertySource, Map<String, Map<String, Object>> keyMap)
      throws IOException {
    DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration();

    ((Map<String, Object>) propertySource.getSource())
        .forEach(
            (key, val) -> {
              if (key.startsWith("spring.cdc[")) {
                String[] items = key.split("\\.");
                String keyName = items[items.length - 1];
                keyMap.compute(
                    items[1],
                    (k, v) -> {
                      if (v == null) {
                        Map<String, Object> childMap = new HashMap<>();
                        childMap.put(keyName, val);
                        return childMap;
                      } else {
                        v.put(keyName, val);
                        return v;
                      }
                    });
              }
            });

    List<Model> models = new ArrayList<>();

    keyMap.forEach(
        (k, v) -> {
          Model model = new Model();
          model.setName(v.get("name").toString());
          model.setMongodb(new MongoProperties());
          Class<? extends MongoProperties> propertiesClass = model.getMongodb().getClass();
          v.forEach(
              (ck, cv) -> {
                if (!ck.equalsIgnoreCase("name")) {
                  Field field = null;
                  try {

                    field = propertiesClass.getDeclaredField(ck);
                    field.setAccessible(true);
                    getValueFromFieldType(field, cv, model.getMongodb());
                  } catch (NoSuchFieldException e) {
                    throw new RuntimeException("No field Found " + ck);
                  } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                  }
                }
              });
          models.add(model);
        });

    databaseConfiguration.setService(models);

    return databaseConfiguration;
  }

  private void getValueFromFieldType(Field field, Object cv, MongoProperties mongodb)
      throws IllegalAccessException {
    final Class<?> type = field.getType();
    Object typedValue = null;
    if (int.class.isAssignableFrom(type) || Integer.class.isAssignableFrom(type)) {
      typedValue = Integer.parseInt(cv.toString());
    } else if (boolean.class.isAssignableFrom(type) || Boolean.class.isAssignableFrom(type)) {
      typedValue = Boolean.valueOf(cv.toString());
    } else if (char.class.arrayType().isAssignableFrom(type)) {
      typedValue = cv.toString().toCharArray();
    } else if (type.isEnum()) {
      typedValue = Enum.valueOf((Class<Enum>) type, cv.toString());
    } else {
      // Assume String
      typedValue = cv.toString();
    }
    field.set(mongodb, typedValue);
  }

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry)
      throws BeansException {
    databaseConfiguration
        .getService()
        .forEach(
            service -> {
              BeanDefinitionBuilder beanDefinitionBuilder =
                  BeanDefinitionBuilder.genericBeanDefinition(
                          MongoClient.class, () -> getBeanOne(service.getMongodb()))
                      .setScope("singleton");
              registry.registerBeanDefinition(
                  service.getName(), beanDefinitionBuilder.getBeanDefinition());
            });
  }

  private MongoClient getBeanOne(MongoProperties mongoProperties) {
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
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory)
      throws BeansException {}
}
