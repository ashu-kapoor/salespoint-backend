package com.ashutosh.cdcservice.lister;

import com.ashutosh.cdcservice.service.queryservice.CustomerQueryService;
import com.ashutosh.cdcservice.service.queryservice.InventoryQueryService;
import com.ashutosh.cdcservice.service.queryservice.SalesQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QueryListener {

  private final InventoryQueryService inventoryQueryService;
  private final CustomerQueryService customerQueryService;
  private final SalesQueryService salesQueryService;

  @EventListener(ApplicationStartedEvent.class)
  public void onMessage() {
    inventoryQueryService.listenToInventory();
    customerQueryService.listenToCustomer();
    salesQueryService.listenToSales();
  }
}
