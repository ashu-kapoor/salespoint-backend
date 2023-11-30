package com.ashutosh.salesservice.service;

import com.ashutosh.saga.framework.Command;
import com.ashutosh.saga.framework.saga.BaseSaga;
import com.ashutosh.saga.ordersaga.OrderSagaCommandMessages;
import com.ashutosh.saga.ordersaga.OrderSagaRequestPayload;
import com.ashutosh.saga.repository.CustomRepository;
import com.ashutosh.salesservice.entity.SalesEntity;
import com.ashutosh.salesservice.exception.ResourceNotFoundException;
import com.ashutosh.salesservice.mapper.SalesEntityMapper;
import com.ashutosh.salesservice.model.CreateSalesRequest;
import com.ashutosh.salesservice.model.CreateSalesResponse;
import com.ashutosh.salesservice.model.GetSalesResponse;
import com.ashutosh.salesservice.saga.OrderSagaChannel;
import com.ashutosh.salesservice.util.LoggingUtil;
import com.ashutosh.salesservice.validations.SalesValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalesService {
  private final CustomRepository<SalesEntity, String> salesRepository;
  private final SalesEntityMapper salesEntityMapper;
  private final SalesValidator salesValidator;

  public Mono<CreateSalesResponse> cancelSales(String itemId) {
    // Semantic lock if sales is not CANCELLED
    // TODO: functionality
    return salesRepository
        .findById(itemId)
        .map(salesEntityMapper::toCreateSalesResponse)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException(itemId)))
        .tap(
            LoggingUtil.<CreateSalesResponse>logTapper(
                new StringBuilder("cancelling sales id ").append(itemId)))
        .contextCapture();
  }

  @Transactional
  public Mono<CreateSalesResponse> createSales(Mono<CreateSalesRequest> createSalesRequest) {

    return createSalesRequest
        .handle(salesValidator)
        .cast(CreateSalesRequest.class)
        .map(salesEntityMapper::toSalesEntity)
        .flatMap(salesRepository::save)
        // .log()
        .map(this::buildSaga)
        .flatMap(salesRepository::updateSaga)
        .doOnError(err -> log.error("Error creating saga for order , error {}", err.getMessage()))
        .doOnSuccess(
            salesEntity -> {
              log.info("Order created successfully with saga {}", salesEntity);
            })
        .map(salesEntityMapper::toCreateSalesResponse);
  }

  public Mono<GetSalesResponse> getSalesById(String itemId) {
    return salesRepository
        .findById(itemId)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException(itemId)))
        .map(salesEntityMapper::toGetSalesResponse)
        .tap(
            LoggingUtil.<GetSalesResponse>logTapper(
                new StringBuilder("fetching order id ").append(itemId)))
        .contextCapture();
  }

  private SalesEntity buildSaga(SalesEntity salesEntity) {
    String id = salesEntity.getId();
    log.info("Generating Saga for order :{}", id);
    OrderSagaRequestPayload orderSagaRequestPayload =
        new OrderSagaRequestPayload(
            id,
            id,
            OrderSagaCommandMessages.DEBIT_INVENTORY.getValue(),
            salesEntity.getCustomerId(),
            salesEntity.getQuantity(),
            salesEntity.getAmount(),
            salesEntity.getProductId());

    BaseSaga<OrderSagaRequestPayload, SalesEntity> salesSaga =
        BaseSaga.Builder.newInstance(salesEntity, orderSagaRequestPayload)
            .withStep(OrderSagaChannel.INVENTORY.toString())
            .processingCommand(new Command(OrderSagaCommandMessages.DEBIT_INVENTORY.getValue()))
            .compensatingCommand(new Command(OrderSagaCommandMessages.REVERT_INVENTORY.getValue()))
            .and()
            .withStep(OrderSagaChannel.CUSTOMER.toString())
            .processingCommand(
                new Command(
                    OrderSagaCommandMessages.DEBIT_CUSTOMER
                        .getValue())) // pivot command no compensation
            .and()
            .build();

    salesEntity.setSaga(salesSaga);

    return salesEntity;
  }
}
