package com.ashutosh.cdcservice.service.queryservice;

import com.ashutosh.saga.kafka.producer.QueryProducer;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.reactivestreams.client.MongoClient;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.bson.Document;
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
public class InventoryQueryService {
  @Resource(name = "inventoryDatabase")
  private MongoClient inventoryMongoClient;

  QueryProducer<Document> producer = new QueryProducer<>("cdc-inventory-query");

  public void listenToInventory() {

    ReactiveMongoTemplate reactiveMongoTemplate =
        new ReactiveMongoTemplate(inventoryMongoClient, "inventorydb");
    Criteria updateCriteria = Criteria.where("operationType").in("update", "replace");
    Criteria insertUpdateCriteria =
        new Criteria().orOperator(Criteria.where("operationType").in("insert"), updateCriteria);

    MatchOperation sagaMatchAggregation = Aggregation.match(insertUpdateCriteria);
    Aggregation aggregation = Aggregation.newAggregation(sagaMatchAggregation);

    // System.out.println(sagaMatchAggregation);

    ChangeStreamOptions options =
        ChangeStreamOptions.builder().filter(aggregation).returnFullDocumentOnUpdate().build();

    ObjectMapper om = new ObjectMapper();
    // om.registerModule(new SimpleModule().addDeserializer(ObjectId.class, new
    // SagaPayloadDeserializer()));
    om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    Flux<ChangeStreamEvent<JsonNode>> salesEntity =
        reactiveMongoTemplate.changeStream("inventoryEntity", options, JsonNode.class);
    salesEntity
        .map(ChangeStreamEvent::getRaw)
        // .doOnNext(System.out::println)
        .map(ChangeStreamDocument::getFullDocument)
        .map(
            x -> {
              sendToKafka(x);
              return x;
            })
        // .doOnNext(System.out::println)
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe();
  }

  private void sendToKafka(Document entity) {
    log.info(
        "Triggering inventory query kafka with topic:{}, payload:{}", "INVENTORY_QUERY", entity);
    producer
        .getSender()
        .send(
            Flux.just(
                SenderRecord.create(
                    new ProducerRecord<>("INVENTORY_QUERY", entity.get("_id").toString(), entity),
                    entity.get("_id").toString())))
        .doOnNext(
            senderResult ->
                log.info(
                    "send record topic:{}, inventory: {}",
                    senderResult.recordMetadata().topic(),
                    entity.get("_id").toString()))
        .subscribe();
  }

  @PreDestroy
  void destroy() {
    producer.close();
    ;
  }
}
