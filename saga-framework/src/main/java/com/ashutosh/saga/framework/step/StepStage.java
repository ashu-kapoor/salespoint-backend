package com.ashutosh.saga.framework.step;

public enum StepStage {
  FORWARD("FORWARD"),
  COMPENSATE("COMPENSATE");

  private String value;

  StepStage(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
