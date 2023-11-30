package com.ashutosh.searchservice.service.handler;

import com.ashutosh.searchservice.queryentity.InventoryQueryEntity;
import com.ashutosh.searchservice.search.SearchRequestDto;
import com.ashutosh.searchservice.service.query.InventoryQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class InventoryHandlerService {

  private final InventoryQueryService inventoryQueryService;

  public Mono<ServerResponse> inventoryIdHandler(ServerRequest serverRequest) {
    return ServerResponse.ok()
        .body(
            inventoryQueryService.searchInventoryById(serverRequest.pathVariable("id")),
            InventoryQueryEntity.class);
  }

  public Mono<ServerResponse> inventoryHandler(ServerRequest serverRequest) {

    return ServerResponse.ok()
        .body(
            inventoryQueryService.searchInventory(
                serverRequest.bodyToMono(SearchRequestDto.class),
                serverRequest.queryParam("filter")),
            InventoryQueryEntity.class);
  }
}
