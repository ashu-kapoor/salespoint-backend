package com.ashutosh.salesservice.controller;

import com.ashutosh.salesservice.model.CreateSalesRequest;
import com.ashutosh.salesservice.model.CreateSalesResponse;
import com.ashutosh.salesservice.model.GetSalesResponse;
import com.ashutosh.salesservice.service.SalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class SalesController implements SalesApi {

  private final SalesService salesService;

  @Override
  public Mono<ResponseEntity<CreateSalesResponse>> cancelSales(
      String itemId, ServerWebExchange exchange) {
    return salesService.cancelSales(itemId).map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<CreateSalesResponse>> createSales(
      Mono<CreateSalesRequest> createSalesRequest, ServerWebExchange exchange) {
    return salesService.createSales(createSalesRequest).map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<GetSalesResponse>> getSalesById(
      String itemId, ServerWebExchange exchange) {
    return salesService.getSalesById(itemId).map(ResponseEntity::ok);
  }
}
