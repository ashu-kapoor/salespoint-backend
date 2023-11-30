package com.ashutosh.salesservice.repository;

import com.ashutosh.saga.repository.CustomRepository;
import com.ashutosh.salesservice.entity.SalesEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SalesRepository implements CustomRepository<SalesEntity, String> {

  private final ReactiveMongoTemplate mongoTemplate;

  @Override
  public <S extends SalesEntity> Mono<S> save(S entity) {
    return mongoTemplate.save(entity);
  }

  @Override
  public <S extends SalesEntity> Mono<S> update(S entity) {
    org.springframework.data.mongodb.core.query.Query query =
        new org.springframework.data.mongodb.core.query.Query();
    query.addCriteria(Criteria.where("id").is(entity.getId()));
    return mongoTemplate.findAndReplace(query, entity);
  }

  @Override
  public Mono<SalesEntity> findById(String s) {
    return mongoTemplate.findById(s, SalesEntity.class);
  }

  @Override
  public Flux<SalesEntity> findAll() {
    return mongoTemplate.findAll(SalesEntity.class);
  }

  @Override
  public Mono<Void> deleteAllById(Iterable<? extends String> strings) {
    org.springframework.data.mongodb.core.query.Query query =
        new org.springframework.data.mongodb.core.query.Query(Criteria.where("id").in(strings));
    return mongoTemplate.remove(query, SalesEntity.class).then();
  }

  @Override
  public Mono<Void> deleteAll() {
    org.springframework.data.mongodb.core.query.Query query =
        new org.springframework.data.mongodb.core.query.Query();
    query.addCriteria(Criteria.where("productId").regex(".*"));
    return mongoTemplate.remove(query, SalesEntity.class).then();
  }

  @Override
  public Mono<Void> deleteById(String s) {
    org.springframework.data.mongodb.core.query.Query query =
        new org.springframework.data.mongodb.core.query.Query();
    query.addCriteria(Criteria.where("id").is(s));
    return mongoTemplate.remove(query, SalesEntity.class).then();
  }

  public Mono<SalesEntity> updateSaga(SalesEntity entity) {
    org.springframework.data.mongodb.core.query.Query query =
        new Query(Criteria.where("id").is(entity.getId()));
    Update update = new Update().set("saga", entity.getSaga());
    return mongoTemplate.findAndModify(query, update, SalesEntity.class);
  }
}
