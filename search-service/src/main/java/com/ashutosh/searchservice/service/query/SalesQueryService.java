package com.ashutosh.searchservice.service.query;

import com.ashutosh.searchservice.exception.ResourceNotFoundException;
import com.ashutosh.searchservice.queryentity.CustomerQueryEntity;
import com.ashutosh.searchservice.queryentity.InventoryQueryEntity;
import com.ashutosh.searchservice.queryentity.SalesQueryEntity;
import com.ashutosh.searchservice.search.SearchRequestDto;
import com.ashutosh.searchservice.search.helper.QueryHelper;
import com.ashutosh.searchservice.util.LoggingUtil;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.BasicBSONObject;
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
public class SalesQueryService {
  private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

  public Mono<Void> processRecord(Document document, String key) {
    /*     Map<String, Object> entityMap = entityToMap(entity);
    //Flatten the document before inserting

    return reactiveElasticsearchTemplate.get(entity.getProductId(), InventoryQueryEntity.class, IndexCoordinates.of("inventory"))
            .zipWith(reactiveElasticsearchTemplate.get(entity.getCustomerId(), CustomerQueryEntity.class, IndexCoordinates.of("customer")))
            .map(tuple->{
                entityMap.put("customer", tuple.getT2());
                entityMap.put("inventory",tuple.getT1());
                return entityMap;
            })
            .flatMap(em-> {
                UpdateQuery updateQuery =   UpdateQuery.builder(entity.get_id()).withDocAsUpsert(true).withDocument(Document.from(em)).build();
                return reactiveElasticsearchTemplate.update(updateQuery, IndexCoordinates.of("sales"));
            })
            .doOnNext(rec-> log.info("Inventory record operation:{} for id:{}",rec.getResult().name(),entity.get_id())).then();*/

    SalesQueryEntity salesQueryEntity = toSalesQueryEntity(document, key);
    return reactiveElasticsearchTemplate
        .get(
            salesQueryEntity.getProductId(),
            InventoryQueryEntity.class,
            IndexCoordinates.of("inventory"))
        .zipWith(
            reactiveElasticsearchTemplate.get(
                salesQueryEntity.getCustomerId(),
                CustomerQueryEntity.class,
                IndexCoordinates.of("customer")))
        .map(
            tuple -> {
              salesQueryEntity.setCustomer(tuple.getT2());
              salesQueryEntity.setInventory(tuple.getT1());
              return salesQueryEntity;
            })
        .flatMap(
            em -> {
              return reactiveElasticsearchTemplate.save(salesQueryEntity);
            })
        .doOnNext(rec -> log.info("Sales record pushed for id:{}", rec.getId()))
        .then();
  }

  private Map<String, Object> entityToMap(SalesQueryEntity entity) {
    Map<String, Object> hashMap = new HashMap<>();
    hashMap.put("id", entity.getId());
    hashMap.put("quantity", entity.getQuantity());
    hashMap.put("saga", entity.getSaga());
    hashMap.put("amount", entity.getAmount());
    hashMap.put("productId", entity.getProductId());
    hashMap.put("status", entity.getStatus());
    hashMap.put("customerId", entity.getCustomerId());
    return hashMap;
  }

  private SalesQueryEntity toSalesQueryEntity(Document x, String key) {
    SalesQueryEntity entity = new SalesQueryEntity();
    // entity.set_id(x.get("_id").toString());
    entity.setId(key);
    entity.setAmount(new BigDecimal(x.getString("amount")));
    entity.setQuantity(x.getInteger("quantity"));

    if (x.get("saga") != null) {
      BasicBSONObject sagaObject = new BasicBSONObject();
      sagaObject.putAll(x.get("saga", LinkedHashMap.class));
      // entity.setSaga(x.get("saga", Document.class));
      entity.setSaga(sagaObject);
    }

    entity.setStatus(x.getString("status"));
    entity.setCustomerId(x.getString("customerId"));
    entity.setProductId(x.getString("productId"));
    return entity;
  }

  public Flux<SalesQueryEntity> searchSales(
      Mono<SearchRequestDto> requestDtoMono, Optional<String> filter) {
    return requestDtoMono
        .map(searchRequestDto -> QueryHelper.buildSearchRequest(searchRequestDto, filter))
        .switchIfEmpty(Mono.just(QueryHelper.buildSearchRequest(null, filter)))
        .flatMapMany(
            q ->
                reactiveElasticsearchTemplate.search(
                    q, SalesQueryEntity.class, IndexCoordinates.of("sales")))
        .map(SearchHit::getContent)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .tap(
            LoggingUtil.<SalesQueryEntity>logTapper(
                new StringBuilder("searching sales by filter ")))
        .contextCapture();
  }

  public Mono<SalesQueryEntity> searchSalesById(String id) {
    return reactiveElasticsearchTemplate
        .get(id, SalesQueryEntity.class)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .tap(
            LoggingUtil.<SalesQueryEntity>logTapper(
                new StringBuilder("fetching sales id ").append(id)))
        .contextCapture();
  }
}
