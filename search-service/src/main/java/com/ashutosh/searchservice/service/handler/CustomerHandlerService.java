package com.ashutosh.searchservice.service.handler;

import com.ashutosh.searchservice.queryentity.CustomerQueryEntity;
import com.ashutosh.searchservice.search.SearchRequestDto;
import com.ashutosh.searchservice.service.query.CustomerQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomerHandlerService {

  private final CustomerQueryService customerQueryService;

  public Mono<ServerResponse> customerIdHandler(ServerRequest serverRequest) {
    return ServerResponse.ok()
        .body(
            customerQueryService.searchCustomerById(serverRequest.pathVariable("id")),
            CustomerQueryEntity.class);
  }

  public Mono<ServerResponse> customerHandler(ServerRequest serverRequest) {

    return ServerResponse.ok()
        .body(
            customerQueryService.searchCustomer(
                serverRequest.bodyToMono(SearchRequestDto.class),
                serverRequest.queryParam("filter")),
            CustomerQueryEntity.class);
  }
}
