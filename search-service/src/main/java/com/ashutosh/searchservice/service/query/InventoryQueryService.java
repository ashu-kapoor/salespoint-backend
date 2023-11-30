package com.ashutosh.searchservice.service.query;

import com.ashutosh.searchservice.exception.ResourceNotFoundException;
import com.ashutosh.searchservice.queryentity.InventoryQueryEntity;
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
public class InventoryQueryService {
  private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

  public Mono<Void> processRecord(Document document, String key) {
    // Map<String, ?> entityMap = entityToMap(entity);
    // UpdateQuery updateQuery =
    // UpdateQuery.builder(entity.get_id()).withDocAsUpsert(true).withDocument(Document.from(entityMap)).build();
    /* return reactiveElasticsearchTemplate.update(updateQuery, IndexCoordinates.of("inventory"))
    .doOnNext(rec-> log.info("Inventory record operation:{} for id:{}",rec.getResult().name(),entity.get_id())).then();*/
    InventoryQueryEntity inventoryQueryEntity = toInventoryQueryEntity(document, key);
    return reactiveElasticsearchTemplate
        .save(inventoryQueryEntity)
        .doOnNext(
            rec -> log.info("Inventory record pushed for id:{}", inventoryQueryEntity.getId()))
        .then();
  }

  private InventoryQueryEntity toInventoryQueryEntity(Document x, String key) {
    InventoryQueryEntity entity = new InventoryQueryEntity();
    entity.setId(key);
    entity.setPrice(new BigDecimal(x.getString("price")));
    entity.setQuantity(x.getInteger("quantity"));
    entity.setProductName(x.getString("productName"));
    return entity;
  }

  public Flux<InventoryQueryEntity> searchInventory(
      Mono<SearchRequestDto> requestDtoMono, Optional<String> filter) {
    return requestDtoMono
        .map(searchRequestDto -> QueryHelper.buildSearchRequest(searchRequestDto, filter))
        .switchIfEmpty(Mono.just(QueryHelper.buildSearchRequest(null, filter)))
        .flatMapMany(
            q ->
                reactiveElasticsearchTemplate.search(
                    q, InventoryQueryEntity.class, IndexCoordinates.of("inventory")))
        .map(SearchHit::getContent)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .tap(
            LoggingUtil.<InventoryQueryEntity>logTapper(
                new StringBuilder("searching inventory by filter ")))
        .contextCapture();
  }

  public Mono<InventoryQueryEntity> searchInventoryById(String id) {
    return reactiveElasticsearchTemplate
        .get(id, InventoryQueryEntity.class)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .tap(
            LoggingUtil.<InventoryQueryEntity>logTapper(
                new StringBuilder("searching inventory by id ").append(id)))
        .contextCapture();
  }
}
