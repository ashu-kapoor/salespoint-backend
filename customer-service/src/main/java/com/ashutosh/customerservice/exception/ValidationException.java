package com.ashutosh.customerservice.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidationException extends RuntimeException {
  private final String message;
  private final Integer subCode = 100;
  private final String type = "Validation";

  public ValidationException(String message) {
    super(message);
    this.message = message;
  }
}
