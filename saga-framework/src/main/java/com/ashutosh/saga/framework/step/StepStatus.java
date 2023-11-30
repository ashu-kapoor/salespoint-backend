package com.ashutosh.saga.framework.step;

public enum StepStatus {
  SUCCESS("SUCCESS"),
  FAILURE("FAILURE");

  private String value;

  StepStatus(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
