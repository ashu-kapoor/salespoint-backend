package com.ashutosh.cdcservice.service.cdcservice;

import com.ashutosh.saga.kafka.producer.SagaParticipantProducer;
import com.ashutosh.saga.ordersaga.OrderSagaResponsePayload;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.OperationType;
import com.mongodb.reactivestreams.client.MongoClient;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.data.mongodb.core.ChangeStreamEvent;
import org.springframework.data.mongodb.core.ChangeStreamOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.sender.SenderRecord;

@Service
@Slf4j
public class CustomerService {

  @Resource(name = "customerDatabase")
  private MongoClient customerMongoClient;

  SagaParticipantProducer<OrderSagaResponsePayload> producer =
      new SagaParticipantProducer<>("cdc-customer-saga");

  public void listenToCustomer() {

    ReactiveMongoTemplate reactiveMongoTemplate =
        new ReactiveMongoTemplate(customerMongoClient, "customerdb");
    Criteria updateCriteria = Criteria.where("operationType").in("update");
    Criteria insertUpdateCriteria =
        new Criteria().orOperator(Criteria.where("operationType").in("insert"), updateCriteria);
    Criteria sagaFilterCriteria =
        new Criteria().andOperator(Criteria.where("saga").exists(true), insertUpdateCriteria);
    MatchOperation sagaMatchAggregation = Aggregation.match(sagaFilterCriteria);
    Aggregation aggregation = Aggregation.newAggregation(sagaMatchAggregation);

    // System.out.println(sagaMatchAggregation);

    ChangeStreamOptions options =
        ChangeStreamOptions.builder().filter(aggregation).returnFullDocumentOnUpdate().build();

    ObjectMapper om = new ObjectMapper();
    // om.registerModule(new SimpleModule().addDeserializer(SagaPayload.class, new
    // SagaPayloadDeserializer()));
    om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    Flux<ChangeStreamEvent<JsonNode>> salesEntity =
        reactiveMongoTemplate.changeStream("customerEntity", options, JsonNode.class);
    salesEntity
        .map(ChangeStreamEvent::getRaw)
        // .doOnNext(System.out::println)
        .filter(
            doc -> {
              if (doc.getOperationType() == OperationType.UPDATE
                  && doc.getUpdateDescription().getUpdatedFields().keySet().stream()
                          .filter(key -> key.startsWith("saga"))
                          .count()
                      > 0) {
                return true;
              }
              return false;
            })
        .map(ChangeStreamDocument::getFullDocument)
        .map(
            x -> {
              return om.convertValue(x.get("saga"), OrderSagaResponsePayload.class);
            })
        .map(
            x -> {
              sendToKafka(x);
              return x;
            })
        // .doOnNext(e->log.info("Sent message to Kafka for customer {}", e))
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe();
  }

  private void sendToKafka(OrderSagaResponsePayload saga) {
    log.info("Triggering customer cdc kafka with topic:{}, payload: {}", "SALES_SAGA", saga);
    producer
        .getSender()
        .send(
            Flux.just(
                SenderRecord.create(
                    new ProducerRecord<>("SALES_SAGA", saga.getSagaId(), saga), saga.getSagaId())))
        .doOnNext(
            senderResult ->
                log.info(
                    "send record topic:{}, saga Id:{}",
                    senderResult.recordMetadata().topic(),
                    saga.getSagaId()))
        .subscribe();
  }

  @PreDestroy
  void destroy() {
    producer.close();
    ;
  }
}
