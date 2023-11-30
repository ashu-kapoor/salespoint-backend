package com.ashutosh.saga.kafka.producer;

import com.ashutosh.saga.framework.saga.SagaResponsePayload;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

@Getter
public class SagaParticipantProducer<T extends SagaResponsePayload> {

  private final KafkaSender<String, T> sender;

  public SagaParticipantProducer(String clientId) {

    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId + UUID.randomUUID());
    props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
    // props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, clientId+"-1");
    props.put(ProducerConfig.ACKS_CONFIG, "all");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    props.put(ProducerConfig.RETRIES_CONFIG, "3");
    SenderOptions<String, T> senderOptions = SenderOptions.create(props);

    sender = KafkaSender.create(senderOptions);
  }

  public void close() {
    sender.close();
  }
}
