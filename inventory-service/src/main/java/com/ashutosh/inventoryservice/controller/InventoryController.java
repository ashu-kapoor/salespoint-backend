package com.ashutosh.inventoryservice.controller;

import com.ashutosh.inventoryservice.model.CreateInventoryRequest;
import com.ashutosh.inventoryservice.model.CreateInventoryResponse;
import com.ashutosh.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class InventoryController implements InventoryApi {
  // private final Jwt oauth2User;
  private final InventoryService inventoryService;

  @Override
  public Mono<ResponseEntity<Void>> deletetinventories(ServerWebExchange exchange) {
    return inventoryService.deleteInventoryItems().map(res -> ResponseEntity.noContent().build());
  }

  @Override
  public Mono<ResponseEntity<Flux<CreateInventoryResponse>>> getinventories(
      ServerWebExchange exchange) {
    return Mono.fromSupplier(() -> ResponseEntity.ok(inventoryService.getInventoryItems()));
  }

  @PreAuthorize("hasAuthority('SalesPoint_AdminRole')")
  @Override
  public Mono<ResponseEntity<CreateInventoryResponse>> createinventory(
      Mono<CreateInventoryRequest> createInventoryRequest, ServerWebExchange exchange) {
    return inventoryService.createInventory(createInventoryRequest).map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<Void>> deleteinventory(String itemId, ServerWebExchange exchange) {
    return inventoryService
        .deleteInventoryItem(itemId)
        .map(res -> ResponseEntity.noContent().build());
  }

  @Override
  public Mono<ResponseEntity<CreateInventoryResponse>> getInventoryById(
      String itemId, ServerWebExchange exchange) {
    return inventoryService.getInventoryById(itemId).map(ResponseEntity::ok);
  }

  @Override
  public Mono<ResponseEntity<CreateInventoryResponse>> updateinventory(
      String itemId,
      Mono<CreateInventoryRequest> createInventoryRequest,
      ServerWebExchange exchange) {
    return inventoryService
        .updateInventoryById(itemId, createInventoryRequest)
        .map(ResponseEntity::ok);
  }
}
