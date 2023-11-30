package com.ashutosh.searchservice.exception;

import lombok.Data;

@Data
public class ErrorResponse {

  private Integer code;

  private String type;

  private String message;
}
