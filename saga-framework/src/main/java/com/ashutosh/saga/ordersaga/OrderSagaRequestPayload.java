package com.ashutosh.saga.ordersaga;

import com.ashutosh.saga.framework.saga.SagaPayload;
import java.math.BigDecimal;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OrderSagaRequestPayload implements SagaPayload {
  private String orderId;
  private String sagaId;
  private String command;
  private String customerId;
  private Integer quantity;
  private BigDecimal amount;
  private String inventoryId;
}
