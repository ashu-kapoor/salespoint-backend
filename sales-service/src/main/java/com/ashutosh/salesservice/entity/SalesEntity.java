package com.ashutosh.salesservice.entity;

import com.ashutosh.saga.framework.saga.BaseSaga;
import com.ashutosh.saga.framework.saga.SagaEntity;
import com.ashutosh.saga.ordersaga.OrderSagaRequestPayload;
import java.math.BigDecimal;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
public class SalesEntity implements SagaEntity {
  @Id private String id;

  @Version private Long version;

  private Integer quantity;

  private String customerId;

  private String productId;

  private SalesStatus status = SalesStatus.PENDING;

  private BigDecimal amount;

  private BaseSaga<OrderSagaRequestPayload, SalesEntity> saga;

  @Override
  public void processCompletion() {
    this.status = SalesStatus.SUCCESS;
  }

  @Override
  public void processFailure() {
    this.status = SalesStatus.FAILED;
  }

  @Override
  public void markProcessing() {
    this.status = SalesStatus.PROCESSING;
  }
}
