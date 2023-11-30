package com.ashutosh.customerservice.repository;

import com.ashutosh.customerservice.entity.CustomerEntity;
import com.ashutosh.saga.repository.CustomRepository;
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
public class CustomerRepository implements CustomRepository<CustomerEntity, String> {

  private final ReactiveMongoTemplate mongoTemplate;

  @Override
  public <S extends CustomerEntity> Mono<S> save(S entity) {
    return mongoTemplate.save(entity);
  }

  @Override
  public Mono<CustomerEntity> update(CustomerEntity entity) {
    /* Query query = new Query();
    query.addCriteria(Criteria.where("id").is(entity.getId()));
    return mongoTemplate.findAndReplace(query,entity);*/
    Query query = new Query(Criteria.where("id").is(entity.getId()));
    Update update =
        new Update()
            .set("firstName", entity.getFirstName())
            .set("lastName", entity.getLastName())
            .set("amount", entity.getAmount())
            .set("email", entity.getEmail());
    return mongoTemplate
        .findAndModify(query, update, CustomerEntity.class)
        .flatMap(e -> findById(e.getId()));
  }

  @Override
  public Mono<CustomerEntity> findById(String s) {
    return mongoTemplate.findById(s, CustomerEntity.class);
  }

  @Override
  public Flux<CustomerEntity> findAll() {
    return mongoTemplate.findAll(CustomerEntity.class);
  }

  @Override
  public Mono<Void> deleteAllById(Iterable<? extends String> strings) {
    Query query = new Query(Criteria.where("id").in(strings));
    return mongoTemplate.remove(query, CustomerEntity.class).then();
  }

  @Override
  public Mono<Void> deleteAll() {
    Query query = new Query();
    query.addCriteria(Criteria.where("firstName").regex(".*"));
    return mongoTemplate.remove(query, CustomerEntity.class).then();
  }

  @Override
  public Mono<Void> deleteById(String s) {
    Query query = new Query();
    query.addCriteria(Criteria.where("id").is(s));
    return mongoTemplate.remove(query, CustomerEntity.class).then();
  }

  public Mono<CustomerEntity> updateSaga(CustomerEntity entity) {
    Query query = new Query(Criteria.where("id").is(entity.getId()));
    Update update =
        new Update()
            .set("saga", entity.getSaga())
            .set("orderProcessedMap", entity.getOrderProcessedMap())
            .set("amount", entity.getAmount());
    return mongoTemplate.findAndModify(query, update, CustomerEntity.class);
  }
}
