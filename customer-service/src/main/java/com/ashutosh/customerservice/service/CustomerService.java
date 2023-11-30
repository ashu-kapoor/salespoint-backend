package com.ashutosh.customerservice.service;

import com.ashutosh.customerservice.entity.CustomerEntity;
import com.ashutosh.customerservice.exception.ResourceNotFoundException;
import com.ashutosh.customerservice.mapper.CustomerEntityMapper;
import com.ashutosh.customerservice.model.CreateCustomerRequest;
import com.ashutosh.customerservice.model.CreateCustomerResponse;
import com.ashutosh.customerservice.util.LoggingUtil;
import com.ashutosh.customerservice.validations.CustomerValidator;
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
public class CustomerService {
  private final CustomRepository<CustomerEntity, String> customerRepository;
  private final CustomerEntityMapper customerEntityMapper;
  private final CustomerValidator customerValidator;

  public Mono<CreateCustomerResponse> createCustomer(
      Mono<CreateCustomerRequest> createCustomerRequest) {
    return createCustomerRequest
        .handle(customerValidator::accept)
        .cast(CreateCustomerRequest.class)
        .map(customerEntityMapper::toCustomerEntity)
        .flatMap(customerRepository::save)
        // .log()
        .map(customerEntityMapper::toCustomerResponse)
        .tap(LoggingUtil.<CreateCustomerResponse>logTapper(new StringBuilder("creating customer")))
        .contextCapture();
  }

  public Mono<CreateCustomerResponse> updateCustomerById(
      String customerId, Mono<CreateCustomerRequest> createCustomerRequest) {
    return customerRepository
        .findById(customerId)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException(customerId)))
        // .doOnError((e)-> System.out.println("Error1"))
        .flatMap(
            p ->
                createCustomerRequest
                    .handle(customerValidator::accept)
                    .doOnError((e) -> System.out.println("Error2"))
                    .map(customerEntityMapper::toCustomerEntity)
                    .doOnNext(e -> e.setId(customerId)))
        .flatMap(customerRepository::update)
        .map(customerEntityMapper::toCustomerResponse)
        .tap(
            LoggingUtil.<CreateCustomerResponse>logTapper(
                new StringBuilder("updating customer id ").append(customerId)))
        .contextCapture();
  }

  public Mono<Void> deleteCustomer(String customerId) {
    return customerRepository
        .deleteById(customerId)
        .tap(
            LoggingUtil.<Void>logTapper(
                new StringBuilder("deleting customer id ").append(customerId)))
        .contextCapture();
  }

  public Mono<Void> deleteCustomers() {
    return customerRepository
        .deleteAll()
        .tap(LoggingUtil.<Void>logTapper(new StringBuilder("deleting customers")))
        .contextCapture();
  }

  public Mono<CreateCustomerResponse> getCustomerById(String customerId) {
    return customerRepository
        .findById(customerId)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException(customerId)))
        .map(customerEntityMapper::toCustomerResponse)
        .tap(
            LoggingUtil.<CreateCustomerResponse>logTapper(
                new StringBuilder("fetching customer id ").append(customerId)))
        .contextCapture();
  }

  public Flux<CreateCustomerResponse> getCustomers() {
    return customerRepository
        .findAll()
        .map(customerEntityMapper::toCustomerResponse)
        .switchIfEmpty(Mono.error(new ResourceNotFoundException()))
        .tap(LoggingUtil.<CreateCustomerResponse>logTapper(new StringBuilder("fetching customers")))
        .contextCapture();
  }

  public Mono<Void> processSagaCommand(OrderSagaRequestPayload orderDetails) {

    return customerRepository
        .findById(orderDetails.getCustomerId())
        .doOnNext(i -> log.info("Customer record found for Saga {}", orderDetails.getSagaId()))
        .filter(getCustomerIdempotencyFilter(orderDetails))
        .doOnNext(
            i ->
                log.info(
                    "Idempotency filter passed for customer saga id {}", orderDetails.getSagaId()))
        // No we will only have record if it is unique
        .map(
            getCommandProcessingFunction(
                orderDetails)) // generate the payload and update entity object
        .flatMap(customerRepository::updateSaga)
        .then();
  }

  private Predicate<CustomerEntity> getCustomerIdempotencyFilter(
      OrderSagaRequestPayload orderDetails) {
    return nullPredicate()
        .or(
            Predicate.not(
                entity ->
                    entity.getOrderProcessedMap().containsKey(orderDetails.getOrderId())
                        && ((Document) entity.getOrderProcessedMap().get(orderDetails.getOrderId()))
                            .containsKey(orderDetails.getCommand())));
  }

  private Predicate<CustomerEntity> nullPredicate() {
    return entity -> entity.getOrderProcessedMap() == null;
  }

  Function<CustomerEntity, CustomerEntity> getCommandProcessingFunction(
      OrderSagaRequestPayload orderDetails) {
    return item -> {
      OrderSagaResponsePayload orderSagaResponsePayload;
      if (OrderSagaCommandMessages.DEBIT_CUSTOMER
          .toString()
          .equalsIgnoreCase(orderDetails.getCommand())) {
        if (item.getAmount().compareTo(orderDetails.getAmount()) < 0) {
          // ERROR cant deduct amount
          log.error(
              "Cant process debit command, not enough balance, saga id:{}",
              orderDetails.getSagaId());
          orderSagaResponsePayload =
              new OrderSagaResponsePayload(
                  orderDetails.getSagaId(),
                  OrderSagaCommandMessages.DEBIT_CUSTOMER,
                  StepStatus.FAILURE,
                  "Not enough balance",
                  orderDetails.getOrderId(),
                  orderDetails.getCustomerId(),
                  orderDetails.getQuantity(),
                  orderDetails.getAmount());
          item.setSaga(orderSagaResponsePayload);
          updateMapDetails(item, orderDetails);

        } else {
          log.info(
              "Processing debit command, enough balance, saga id:{}", orderDetails.getSagaId());
          orderSagaResponsePayload =
              new OrderSagaResponsePayload(
                  orderDetails.getSagaId(),
                  OrderSagaCommandMessages.DEBIT_CUSTOMER,
                  StepStatus.SUCCESS,
                  "Customer amount debited",
                  orderDetails.getOrderId(),
                  orderDetails.getCustomerId(),
                  orderDetails.getQuantity(),
                  orderDetails.getAmount());
          item.setSaga(orderSagaResponsePayload);
          item.setAmount(item.getAmount().subtract(orderDetails.getAmount()));
          updateMapDetails(item, orderDetails);
        }
      }

      return item;
    };
  }

  private void updateMapDetails(CustomerEntity item, OrderSagaRequestPayload orderDetails) {
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
