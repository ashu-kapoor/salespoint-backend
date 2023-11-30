package com.ashutosh.saga.framework.saga;

public interface SagaEntity {
  public String getId();

  public void processCompletion();

  public void processFailure();

  public void markProcessing();

  public Saga getSaga();

  public SagaEntityStatus getStatus();
}
