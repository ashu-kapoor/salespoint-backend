package com.ashutosh.cdcservice.lister;

import com.ashutosh.cdcservice.service.cdcservice.CustomerService;
import com.ashutosh.cdcservice.service.cdcservice.InventoryService;
import com.ashutosh.cdcservice.service.cdcservice.SagaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CdcListener {

  private final SagaService sagaService;
  private final InventoryService inventoryService;
  private final CustomerService customerService;

  @EventListener(ApplicationStartedEvent.class)
  public void onMessage() {
    log.info("Starting Saga, Inventory, Customer CDC listeners");
    sagaService.listenToSaga();
    inventoryService.listenToInventory();
    customerService.listenToCustomer();
  }
}
