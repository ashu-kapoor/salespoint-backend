package com.ashutosh.saga.ordersaga;

import com.ashutosh.saga.framework.saga.SagaResponsePayload;
import com.ashutosh.saga.framework.step.StepStatus;
import java.math.BigDecimal;
import lombok.*;

@Getter
@ToString
@Setter
@NoArgsConstructor
public class OrderSagaResponsePayload implements SagaResponsePayload {
  private String orderId;
  private String customerId;
  private Integer quantity;
  private BigDecimal amount;
  private String sagaId;
  private OrderSagaCommandMessages command;
  private StepStatus status;
  private String reason;

  public OrderSagaResponsePayload(
      String sagaId,
      OrderSagaCommandMessages command,
      StepStatus status,
      String reason,
      String orderId,
      String customerId,
      Integer quantity,
      BigDecimal amount) {
    this.sagaId = sagaId;
    this.command = command;
    this.status = status;
    this.reason = reason;
    this.orderId = orderId;
    this.amount = amount;
    this.quantity = quantity;
    this.customerId = customerId;
  }
}
