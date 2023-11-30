package com.ashutosh.inventoryservice.repository;

import com.ashutosh.inventoryservice.entity.InventoryEntity;
import com.ashutosh.saga.repository.CustomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.*;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class InventoryRepository implements CustomRepository<InventoryEntity, String> {

  private final ReactiveMongoTemplate mongoTemplate;

  @Override
  public <S extends InventoryEntity> Mono<S> save(S entity) {
    return mongoTemplate.save(entity);
  }

  @Override
  public Mono<InventoryEntity> update(InventoryEntity entity) {
    /*Query query = new Query();
    query.addCriteria(Criteria.where("id").is(entity.getId()));
    return mongoTemplate.findAndReplace(query,entity);*/
    Query query = new Query(Criteria.where("id").is(entity.getId()));
    Update update =
        new Update()
            .set("quantity", entity.getQuantity())
            .set("price", entity.getPrice())
            .set("productName", entity.getProductName());
    return mongoTemplate
        .findAndModify(query, update, InventoryEntity.class)
        .flatMap(e -> findById(e.getId()));
  }

  @Override
  public Mono<InventoryEntity> findById(String s) {
    return mongoTemplate.findById(s, InventoryEntity.class);
  }

  @Override
  public Flux<InventoryEntity> findAll() {
    return mongoTemplate.findAll(InventoryEntity.class);
  }

  @Override
  public Mono<Void> deleteAllById(Iterable<? extends String> strings) {
    Query query = new Query(Criteria.where("id").in(strings));
    return mongoTemplate.remove(query, InventoryEntity.class).then();
  }

  @Override
  public Mono<Void> deleteAll() {
    Query query = new Query();
    query.addCriteria(Criteria.where("productName").regex(".*"));
    return mongoTemplate.remove(query, InventoryEntity.class).then();
  }

  @Override
  public Mono<Void> deleteById(String s) {
    Query query = new Query();
    query.addCriteria(Criteria.where("id").is(s));
    return mongoTemplate.remove(query, InventoryEntity.class).then();
  }

  public Mono<InventoryEntity> updateSaga(InventoryEntity entity) {
    Query query = new Query(Criteria.where("id").is(entity.getId()));
    Update update =
        new Update()
            .set("saga", entity.getSaga())
            .set("orderProcessedMap", entity.getOrderProcessedMap())
            .set("quantity", entity.getQuantity());
    return mongoTemplate.findAndModify(query, update, InventoryEntity.class);
  }
}
