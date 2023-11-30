package com.ashutosh.salesservice.saga;

public enum OrderSagaChannel {
  INVENTORY("inventory"),
  CUSTOMER("customer");

  private String value;

  OrderSagaChannel(String value) {
    this.value = value;
  }
}
