package com.ashutosh.searchservice.service.query;

import com.ashutosh.searchservice.exception.ResourceNotFoundException;
import com.ashutosh.searchservice.queryentity.CustomerQueryEntity;
import com.ashutosh.searchservice.search.SearchRequestDto;
import com.ashutosh.searchservice.search.helper.QueryHelper;
import com.ashutosh.searchservice.util.LoggingUtil;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.elasticsearch.client.elc.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerQueryService {
  private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

  public Mono<Void> processRecord(Document document, String key) {
    CustomerQueryEntity customerQueryEntity = toCustomerQueryEntity(document, key);
    return reactiveElasticsearchTemplate
        .save(customerQueryEntity)
        .doOnNext(rec -> log.info("Customer record pushed for id:{}", rec.getId()))
        .then();

    /*return reactiveElasticsearchTemplate.update(updateQuery, IndexCoordinates.of("customer"))
    .doOnNext(rec-> log.info("Inventory record operation:{} for id:{}",rec.getResult().name(),entity.get_id())).then();*/
  }

  private CustomerQueryEntity toCustomerQueryEntity(Document x, String key) {
    CustomerQueryEntity entity = new CustomerQueryEntity();
    // entity.set_id(x.get("_id").toString());
    entity.setId(key);
    entity.setAmount(new BigDecimal(x.getString("amount")));
    entity.setEmail(x.getString("email"));
    entity.setLastName(x.getString("lastName"));
    entity.setFirstName(x.getString("firstName"));
    return entity;
  }

  public Flux<CustomerQueryEntity> searchCustomer(
      Mono<SearchRequestDto> requestDtoMono, Optional<String> filter) {
    return requestDtoMono
        .map(searchRequestDto -> QueryHelper.buildSearchRequest(searchRequestDto, filter))
        .switchIfEmpty(Mono.just(QueryHelper.buildSearchRequest(null, filter)))
        .flatMapMany(
            q ->
                reactiveElasticsearchTemplate.search(
                    q, CustomerQueryEntity.class, IndexCoordinates.of("customer")))
        .map(SearchHit::getContent)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .tap(
            LoggingUtil.<CustomerQueryEntity>logTapper(
                new StringBuilder("fetching customers by filter")))
        .contextCapture();
  }

  public Mono<CustomerQueryEntity> searchCustomerById(String id) {
    return reactiveElasticsearchTemplate
        .get(id, CustomerQueryEntity.class)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .tap(
            LoggingUtil.<CustomerQueryEntity>logTapper(
                new StringBuilder("searching customer id ").append(id)))
        .contextCapture();
  }
}
