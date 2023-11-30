package com.ashutosh.saga.framework;

import com.ashutosh.saga.framework.saga.SagaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SagaRepository<T extends SagaEntity> {

  private final Class<T> clazz;

  private final ReactiveMongoTemplate reactiveMongoTemplate;

  public Mono<T> findById(String s) {
    return reactiveMongoTemplate.findById(s, clazz);
  }

  public <S extends T> Mono<S> updateSaga(S entity) {
    org.springframework.data.mongodb.core.query.Query query =
        new Query(Criteria.where("id").is(entity.getId()));
    Update update = new Update().set("saga", entity.getSaga()).set("status", entity.getStatus());
    return (Mono<S>) reactiveMongoTemplate.findAndModify(query, update, entity.getClass());
  }
}
