package com.ashutosh.salesservice.entity;

import com.ashutosh.saga.framework.saga.SagaEntityStatus;

public enum SalesStatus implements SagaEntityStatus {
  PENDING,
  PROCESSING,
  FAILED,
  SUCCESS
}
