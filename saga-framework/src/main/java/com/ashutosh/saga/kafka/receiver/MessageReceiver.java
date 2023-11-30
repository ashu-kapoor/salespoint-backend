package com.ashutosh.saga.kafka.receiver;

import com.ashutosh.saga.framework.saga.SagaPayload;
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
public class MessageReceiver<T extends SagaPayload> {

  private final KafkaReceiver<String, T> receiver;

  public MessageReceiver(String topic, String clientId) {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "inventory-service");
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    // props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 15000*60); //TODO update later on
    // props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 14000*60); //TODO update later on
    props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.ashutosh.saga.ordersaga");
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
