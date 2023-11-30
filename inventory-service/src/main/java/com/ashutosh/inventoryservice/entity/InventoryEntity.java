package com.ashutosh.inventoryservice.entity;

import com.ashutosh.saga.ordersaga.OrderSagaResponsePayload;
import java.math.BigDecimal;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class InventoryEntity {

  @Version private Long version;

  @Id private String id;

  private Integer quantity;

  private String productName;

  private BigDecimal price;

  // OrderId, (Command,Payload)
  // private Map<String, Map<String, DateTime>> orderProcessedMap= new HashMap<>();

  // private String orderProcessedMap;

  private org.bson.Document orderProcessedMap;

  private OrderSagaResponsePayload saga;
}
