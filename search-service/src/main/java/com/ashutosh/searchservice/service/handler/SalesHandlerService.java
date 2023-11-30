package com.ashutosh.searchservice.service.handler;

import com.ashutosh.searchservice.queryentity.SalesQueryEntity;
import com.ashutosh.searchservice.search.SearchRequestDto;
import com.ashutosh.searchservice.service.query.SalesQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SalesHandlerService {

  private final SalesQueryService salesQueryService;

  public Mono<ServerResponse> salesIdHandler(ServerRequest serverRequest) {
    return ServerResponse.ok()
        .body(
            salesQueryService.searchSalesById(serverRequest.pathVariable("id")),
            SalesQueryEntity.class);
  }

  public Mono<ServerResponse> salesHandler(ServerRequest serverRequest) {

    return ServerResponse.ok()
        .body(
            salesQueryService.searchSales(
                serverRequest.bodyToMono(SearchRequestDto.class),
                serverRequest.queryParam("filter")),
            SalesQueryEntity.class);
  }
}
