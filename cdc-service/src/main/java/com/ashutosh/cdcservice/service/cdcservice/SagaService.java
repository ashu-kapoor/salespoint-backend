package com.ashutosh.cdcservice.service.cdcservice;

import com.ashutosh.cdcservice.om.SagaPayloadDeserializer;
import com.ashutosh.saga.framework.saga.BaseSaga;
import com.ashutosh.saga.framework.saga.Saga;
import com.ashutosh.saga.framework.saga.SagaPayload;
import com.ashutosh.saga.kafka.producer.SagaProducer;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.OperationType;
import com.mongodb.reactivestreams.client.*;
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
public class SagaService {
  private final String connectionUrl =
      "mongodb://ashutosh:secretpassword@localhost:30001/?authSource=admin&replicaSet=rs0";

  @Resource(name = "salesDatabase")
  private MongoClient salesMongoClient;

  SagaProducer producer = new SagaProducer("cdc-saga");

  public void listenToSaga() {

    // ConnectionString connectionString = new ConnectionString(connectionUrl);
    // MongoClientSettings clientSettings =
    // MongoClientSettings.builder().applyConnectionString(connectionString).build();
    // try(MongoClient mongoClient = MongoClients.create(clientSettings)){
    ReactiveMongoTemplate reactiveMongoTemplate =
        new ReactiveMongoTemplate(salesMongoClient, "salesdb");
    /*MongoDatabase db = mongoClient.getDatabase("salesdb");
    MongoCollection<Document> salesEntity = db.getCollection("salesEntity");
    ChangeStreamPublisher<Document> documentChangeStreamPublisher = salesEntity.watch();
    Flux.from(documentChangeStreamPublisher).doOnNext(doc->System.out.println(doc)).subscribe();*/

    /*ChangeStreamOptions options = ChangeStreamOptions.builder().filter().build();
    Flux<ChangeStreamEvent<JsonNode>> salesEntity = reactiveMongoTemplate.changeStream("salesEntity", options, JsonNode.class);
    salesEntity.doOnNext(doc->System.out.println(doc)).subscribe();*/

    /*Criteria matchOperation = Criteria.where("saga").exists(true);
    Document document = new Document("$match", matchOperation);

    ChangeStreamOptions options = ChangeStreamOptions.builder().filter(document).build();*/

    Criteria updateCriteria = Criteria.where("operationType").in("update");
    // Criteria sagaUpdateCriteria = new Criteria().andOperator(updateCriteria,
    // Criteria.where("updateDescription.updatedFields.saga.currentStep").exists(true));//.and("updateDescription.updatedFields.saga.currentStep").exists(true);
    Criteria insertUpdateCriteria =
        new Criteria().orOperator(Criteria.where("operationType").in("replace"), updateCriteria);
    Criteria sagaFilterCriteria =
        new Criteria().andOperator(Criteria.where("saga").exists(true), insertUpdateCriteria);
    MatchOperation sagaMatchAggregation = Aggregation.match(sagaFilterCriteria);
    Aggregation aggregation = Aggregation.newAggregation(sagaMatchAggregation);

    // System.out.println(sagaMatchAggregation);

    ChangeStreamOptions options =
        ChangeStreamOptions.builder().filter(aggregation).returnFullDocumentOnUpdate().build();

    ObjectMapper om = new ObjectMapper();
    om.registerModule(
        new SimpleModule().addDeserializer(SagaPayload.class, new SagaPayloadDeserializer()));
    om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    Flux<ChangeStreamEvent<JsonNode>> salesEntity =
        reactiveMongoTemplate.changeStream("salesEntity", options, JsonNode.class);
    salesEntity
        .map(ChangeStreamEvent::getRaw)
        // .doOnNext(System.out::println)
        .filter(
            doc -> {
              if (doc.getOperationType() == OperationType.REPLACE) {
                return true;
              } else if (doc.getOperationType() == OperationType.UPDATE
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
              return om.convertValue(x.get("saga"), BaseSaga.class);
            })
        .map(
            x -> {
              sendToKafka(x);
              return x;
            })
        // .doOnNext(System.out::println)
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe();

    // }
  }

  private void sendToKafka(Saga saga) {
    log.info(
        "Triggering saga kafka with topic:{}, payload:{}",
        saga.getCurrentChannel(),
        saga.getPayload());
    String topic = saga.getCurrentChannel();
    producer
        .getSender()
        .send(
            Flux.just(
                SenderRecord.create(
                    new ProducerRecord<>(topic, saga.getPayload().getSagaId(), saga.getPayload()),
                    saga.getPayload().getSagaId())))
        .doOnNext(
            senderResult ->
                log.info(
                    "send record topic:{}, saga:{}",
                    senderResult.recordMetadata().topic(),
                    saga.getId()))
        .subscribe();
  }

  @PreDestroy
  void destroy() {
    producer.close();
    ;
  }
}
