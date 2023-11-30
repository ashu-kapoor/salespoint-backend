package com.ashutosh.searchservice.search.helper;

public enum Operations {
  GREATER_THAN(">"),
  LESS_THAN("<");

  private String value;

  Operations(String value) {
    this.value = value;
  }

  public String getValue() {
    return this.value;
  }
}
