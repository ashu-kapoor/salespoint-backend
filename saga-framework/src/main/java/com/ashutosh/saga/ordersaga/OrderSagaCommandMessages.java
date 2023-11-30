package com.ashutosh.saga.ordersaga;

public enum OrderSagaCommandMessages {
  DEBIT_INVENTORY("DEBIT_INVENTORY"),
  DEBIT_CUSTOMER("DEBIT_CUSTOMER"),
  REVERT_INVENTORY("REVERT_INVENTORY");

  private String value;

  OrderSagaCommandMessages(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
