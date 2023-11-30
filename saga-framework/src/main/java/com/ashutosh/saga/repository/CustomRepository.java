package com.ashutosh.saga.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomRepository<T, ID> {

  <S extends T> Mono<S> save(S entity);

  Mono<T> findById(ID id);

  Flux<T> findAll();

  Mono<Void> deleteAllById(Iterable<? extends ID> ids);

  Mono<Void> deleteAll();

  Mono<Void> deleteById(ID id);

  Mono<T> updateSaga(T inventoryEntity);

  <S extends T> Mono<S> update(S entity);
}
