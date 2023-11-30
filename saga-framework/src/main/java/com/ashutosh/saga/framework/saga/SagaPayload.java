package com.ashutosh.saga.framework.saga;

public interface SagaPayload {
  String getSagaId();

  void setSagaId(String sagaId);

  String getCommand();

  void setCommand(String command);
}
