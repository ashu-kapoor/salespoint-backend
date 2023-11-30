package com.ashutosh.searchservice.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourceNotFoundException extends RuntimeException {
  public static final String NO_RECORD_FOUND = "No record found";
  public static final String RECORD_NOT_FOUND = "Record not found:";
  private final String message;
  private final Integer subCode = 200;
  private final String type = "Integrity";

  public ResourceNotFoundException(String message) {
    super(RECORD_NOT_FOUND + message);
    this.message = RECORD_NOT_FOUND + message;
  }

  public ResourceNotFoundException() {
    super(NO_RECORD_FOUND);
    this.message = NO_RECORD_FOUND;
  }
}
