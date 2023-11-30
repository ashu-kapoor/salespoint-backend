package com.ashutosh.customerservice.controller;

import com.ashutosh.customerservice.model.CreateCustomerRequest;
import com.ashutosh.customerservice.model.CreateCustomerResponse;
import com.ashutosh.customerservice.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class CustomerController implements CustomerApi {

  private final CustomerService customerService;

  @Override
  public Mono<ResponseEntity<CreateCustomerResponse>> createCustomer(
      Mono<CreateCustomerRequest> createCustomerRequest, ServerWebExchange exchange) {
    return customerService.createCustomer(createCustomerRequest).map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<Void>> deleteCustomer(String customerId, ServerWebExchange exchange) {
    return customerService
        .deleteCustomer(customerId)
        .map(res -> ResponseEntity.noContent().build());
  }

  @Override
  public Mono<ResponseEntity<Void>> deletetCustomers(ServerWebExchange exchange) {
    return customerService.deleteCustomers().map(res -> ResponseEntity.noContent().build());
  }

  @Override
  public Mono<ResponseEntity<CreateCustomerResponse>> getCustomerById(
      String customerId, ServerWebExchange exchange) {
    return customerService.getCustomerById(customerId).map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<Flux<CreateCustomerResponse>>> getCustomers(
      ServerWebExchange exchange) {
    return Mono.fromSupplier(() -> ResponseEntity.ok(customerService.getCustomers()));
  }

  @Override
  public Mono<ResponseEntity<CreateCustomerResponse>> updateCustomer(
      String customerId,
      Mono<CreateCustomerRequest> createCustomerRequest,
      ServerWebExchange exchange) {
    return customerService
        .updateCustomerById(customerId, createCustomerRequest)
        .map(ResponseEntity::ok);
  }
}
