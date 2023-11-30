package com.ashutosh.searchservice.controller;

import com.ashutosh.searchservice.exception.ErrorResponse;
import com.ashutosh.searchservice.exception.ResourceNotFoundException;
import com.ashutosh.searchservice.service.handler.CustomerHandlerService;
import com.ashutosh.searchservice.service.handler.InventoryHandlerService;
import com.ashutosh.searchservice.service.handler.SalesHandlerService;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
public class SearchController {

  private final InventoryHandlerService inventoryHandlerService;
  private final CustomerHandlerService customerHandlerService;
  private final SalesHandlerService salesHandlerService;

  @Bean
  public RouterFunction<ServerResponse> searchServiceController() {
    return RouterFunctions.route()
        .path("search", this::serverResponseHandler)
        .onError(ResourceNotFoundException.class, noRecordExceptionHandler())
        .build();
  }

  private RouterFunction<ServerResponse> serverResponseHandler() {
    return RouterFunctions.route()
        .GET("inventory/{id}", inventoryHandlerService::inventoryIdHandler)
        .GET("inventory", inventoryHandlerService::inventoryHandler)
        .GET("customer/{id}", customerHandlerService::customerIdHandler)
        .GET("customer", customerHandlerService::customerHandler)
        .GET("sales/{id}", salesHandlerService::salesIdHandler)
        .GET("sales", salesHandlerService::salesHandler)
        .build();
  }

  private BiFunction<Throwable, ServerRequest, Mono<ServerResponse>> noRecordExceptionHandler() {
    return (err, serverRequest) -> {
      ResourceNotFoundException ex = (ResourceNotFoundException) err;
      ErrorResponse error = new ErrorResponse();
      error.setCode(ex.getSubCode());
      error.setType(ex.getType());
      error.setMessage(ex.getMessage());
      return ServerResponse.ok().bodyValue(error);
    };
  }
}
