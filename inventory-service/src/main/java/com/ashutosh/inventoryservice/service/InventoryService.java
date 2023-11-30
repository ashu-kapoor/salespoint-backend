package com.ashutosh.inventoryservice.service;

import com.ashutosh.inventoryservice.entity.InventoryEntity;
import com.ashutosh.inventoryservice.exception.ResourceNotFoundException;
import com.ashutosh.inventoryservice.mapper.InventoryEntityMapper;
import com.ashutosh.inventoryservice.model.CreateInventoryRequest;
import com.ashutosh.inventoryservice.model.CreateInventoryResponse;
import com.ashutosh.inventoryservice.util.LoggingUtil;
import com.ashutosh.inventoryservice.validations.InventoryValidator;
import com.ashutosh.saga.framework.step.StepStatus;
import com.ashutosh.saga.ordersaga.OrderSagaCommandMessages;
import com.ashutosh.saga.ordersaga.OrderSagaRequestPayload;
import com.ashutosh.saga.ordersaga.OrderSagaResponsePayload;
import com.ashutosh.saga.repository.CustomRepository;
import java.time.Instant;
import java.util.Date;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
  private final CustomRepository<InventoryEntity, String> inventoryRepository;
  private final InventoryEntityMapper inventoryEntityMapper;
  private final InventoryValidator inventoryValidator;

  public Mono<CreateInventoryResponse> createInventory(
      Mono<CreateInventoryRequest> createInventoryRequest) {
    return createInventoryRequest
        .handle(inventoryValidator)
        .cast(CreateInventoryRequest.class)
        .map(inventoryEntityMapper::toInventoryEntity)
        .flatMap(inventoryRepository::save)
        // .log()
        .map(inventoryEntityMapper::toInventoryResponse)
        .tap(
            LoggingUtil.<CreateInventoryResponse>logTapper(new StringBuilder("creating inventory")))
        .contextCapture();
  }

  public Mono<CreateInventoryResponse> updateInventoryById(
      String inventoryId, Mono<CreateInventoryRequest> createInventoryRequest) {
    return inventoryRepository
        .findById(inventoryId)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException(inventoryId)))
        .flatMap(p -> createInventoryRequest.handle(inventoryValidator))
        .map(inventoryEntityMapper::toInventoryEntity)
        .doOnNext(e -> e.setId(inventoryId))
        .flatMap(inventoryRepository::update)
        .map(inventoryEntityMapper::toInventoryResponse)
        .tap(
            LoggingUtil.<CreateInventoryResponse>logTapper(
                new StringBuilder("updating inventory id ").append(inventoryId)))
        .contextCapture();
  }

  public Mono<Void> deleteInventoryItem(String inventoryId) {
    return inventoryRepository
        .deleteById(inventoryId)
        .tap(
            LoggingUtil.<Void>logTapper(
                new StringBuilder("deleting inventory id ").append(inventoryId)))
        .contextCapture();
  }

  public Mono<Void> deleteInventoryItems() {
    return inventoryRepository
        .deleteAll()
        .tap(LoggingUtil.<Void>logTapper(new StringBuilder("deleting inventories")))
        .contextCapture();
  }

  public Mono<CreateInventoryResponse> getInventoryById(String inventoryId) {
    return inventoryRepository
        .findById(inventoryId)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException(inventoryId)))
        .map(inventoryEntityMapper::toInventoryResponse)
        .tap(
            LoggingUtil.<CreateInventoryResponse>logTapper(
                new StringBuilder("fetching Inventory Id ").append(inventoryId)))
        .contextCapture();
    // .contextWrite(Context.of("correlationId", "testCorrelation"));
  }

  public Flux<CreateInventoryResponse> getInventoryItems() {
    return inventoryRepository
        .findAll()
        .map(inventoryEntityMapper::toInventoryResponse)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .tap(
            LoggingUtil.<CreateInventoryResponse>logTapper(
                new StringBuilder("fetching Inventories")))
        .contextCapture();
  }

  public Mono<Void> processSagaCommand(OrderSagaRequestPayload orderDetails) {

    return inventoryRepository
        .findById(orderDetails.getInventoryId())
        .doOnNext(i -> log.info("Inventory record found for Saga {}", orderDetails.getSagaId()))
        .filter(getInventoryIdempotencyFilter(orderDetails))
        .doOnNext(
            i ->
                log.info(
                    "Idempotency filter passed for inventory saga id {}", orderDetails.getSagaId()))
        // No we will only have record if it is unique
        .map(
            getCommandProcessingFunction(
                orderDetails)) // generate the payload and update entity object
        .flatMap(inventoryRepository::updateSaga)
        .then();
  }

  private Predicate<InventoryEntity> getInventoryIdempotencyFilter(
      OrderSagaRequestPayload orderDetails) {
    return nullPredicate()
        .or(
            Predicate.not(
                inventory ->
                    inventory.getOrderProcessedMap().containsKey(orderDetails.getOrderId())
                        && ((Document)
                                inventory.getOrderProcessedMap().get(orderDetails.getOrderId()))
                            .containsKey(orderDetails.getCommand())));
  }

  private Predicate<InventoryEntity> nullPredicate() {
    return inventory -> inventory.getOrderProcessedMap() == null;
  }

  Function<InventoryEntity, InventoryEntity> getCommandProcessingFunction(
      OrderSagaRequestPayload orderDetails) {
    return item -> {
      OrderSagaResponsePayload orderSagaResponsePayload;
      if (OrderSagaCommandMessages.DEBIT_INVENTORY
          .toString()
          .equalsIgnoreCase(orderDetails.getCommand())) {
        if (item.getQuantity() < orderDetails.getQuantity()) {
          // ERROR cant deduct quantity
          log.error(
              "Cant process debit command, not enough quantity, saga id:{}",
              orderDetails.getSagaId());
          orderSagaResponsePayload =
              new OrderSagaResponsePayload(
                  orderDetails.getSagaId(),
                  OrderSagaCommandMessages.DEBIT_INVENTORY,
                  StepStatus.FAILURE,
                  "Not enough quantity",
                  orderDetails.getOrderId(),
                  orderDetails.getCustomerId(),
                  orderDetails.getQuantity(),
                  orderDetails.getAmount());
          item.setSaga(orderSagaResponsePayload);
          updateMapDetails(item, orderDetails);

        } else {
          log.info(
              "Processing debit command, enough quantity, saga id:{}", orderDetails.getSagaId());
          orderSagaResponsePayload =
              new OrderSagaResponsePayload(
                  orderDetails.getSagaId(),
                  OrderSagaCommandMessages.DEBIT_INVENTORY,
                  StepStatus.SUCCESS,
                  "InventoryDebited",
                  orderDetails.getOrderId(),
                  orderDetails.getCustomerId(),
                  orderDetails.getQuantity(),
                  orderDetails.getAmount());
          item.setSaga(orderSagaResponsePayload);
          item.setQuantity(item.getQuantity() - orderDetails.getQuantity());
          updateMapDetails(item, orderDetails);
        }
      } else if (OrderSagaCommandMessages.REVERT_INVENTORY
          .toString()
          .equalsIgnoreCase(orderDetails.getCommand())) {
        log.info("Reverting debit command,  saga id:{}", orderDetails.getSagaId());
        orderSagaResponsePayload =
            new OrderSagaResponsePayload(
                orderDetails.getSagaId(),
                OrderSagaCommandMessages.REVERT_INVENTORY,
                StepStatus.SUCCESS,
                "InventoryCredited",
                orderDetails.getOrderId(),
                orderDetails.getCustomerId(),
                orderDetails.getQuantity(),
                orderDetails.getAmount());
        item.setSaga(orderSagaResponsePayload);
        item.setQuantity(item.getQuantity() + orderDetails.getQuantity());
        updateMapDetails(item, orderDetails);
      }

      return item;
    };
  }

  private void updateMapDetails(InventoryEntity item, OrderSagaRequestPayload orderDetails) {
    if (item.getOrderProcessedMap() == null) {
      item.setOrderProcessedMap(new Document());
    }
    item.getOrderProcessedMap()
        .compute(
            orderDetails.getOrderId(),
            (k, v) -> {
              v = v == null ? new Document() : v;
              ((Document) v)
                  .computeIfAbsent(orderDetails.getCommand(), cv -> Date.from(Instant.now()));
              return v;
            });
  }
}
