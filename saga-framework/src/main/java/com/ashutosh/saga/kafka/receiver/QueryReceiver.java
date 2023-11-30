package com.ashutosh.saga.kafka.receiver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

@Getter
public class QueryReceiver<T> {

  private final KafkaReceiver<String, T> receiver;

  public QueryReceiver(String topic, String clientId) {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "inventory-service");
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    // props.put(JsonDeserializer.TRUSTED_PACKAGES,
    // "com.ashutosh.saga.ordersaga.queryentity,org.bson.Document");
    props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
    // props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
    props.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId + UUID.randomUUID());
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    ReceiverOptions<String, T> receiverOptions = ReceiverOptions.create(props);
    ReceiverOptions<String, T> subscription =
        receiverOptions.subscription(Collections.singleton(topic));
    receiver = KafkaReceiver.create(subscription);
  }
}
