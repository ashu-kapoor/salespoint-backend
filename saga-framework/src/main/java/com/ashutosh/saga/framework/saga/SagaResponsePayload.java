package com.ashutosh.saga.framework.saga;

import com.ashutosh.saga.framework.step.StepStatus;
import com.ashutosh.saga.ordersaga.OrderSagaCommandMessages;
import lombok.*;

public interface SagaResponsePayload {
  String getSagaId();

  OrderSagaCommandMessages getCommand();

  StepStatus getStatus();

  String getReason();
}
