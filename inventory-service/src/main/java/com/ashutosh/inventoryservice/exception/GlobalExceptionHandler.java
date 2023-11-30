package com.ashutosh.inventoryservice.exception;

import com.ashutosh.inventoryservice.model.ModelApiResponse;
import com.mongodb.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public final Mono<ResponseEntity<ModelApiResponse>> handleResourceException(
      ResourceNotFoundException ex, ServerWebExchange serverWebExchange) {
    ModelApiResponse error = new ModelApiResponse();
    error.setCode(ex.getSubCode());
    error.setType(ex.getType());
    error.setMessage(ex.getMessage());
    return Mono.just(new ResponseEntity<>(error, HttpStatus.NOT_FOUND));
  }

  @ExceptionHandler(ValidationException.class)
  public final Mono<ResponseEntity<ModelApiResponse>> handleValidationException(
      ValidationException ex, ServerWebExchange serverWebExchange) {
    ModelApiResponse error = new ModelApiResponse();
    error.setCode(ex.getSubCode());
    error.setType(ex.getType());
    error.setMessage(ex.getMessage());
    return Mono.just(new ResponseEntity<>(error, HttpStatus.BAD_REQUEST));
  }

  @ExceptionHandler(DuplicateKeyException.class)
  public final Mono<ResponseEntity<ModelApiResponse>> handleDuplicateKeyException(
      DuplicateKeyException ex, ServerWebExchange serverWebExchange) {
    ModelApiResponse error = new ModelApiResponse();
    error.setCode(200);
    error.setType("Integrity");
    error.setMessage(ex.getMessage());
    return Mono.just(new ResponseEntity<>(error, HttpStatus.BAD_REQUEST));
  }

  // @ExceptionHandler(WebExchangeBindException.class)
  public final Mono<ResponseEntity<ModelApiResponse>> handleDuplicateKeyException(
      WebExchangeBindException ex, ServerWebExchange serverWebExchange) {
    ModelApiResponse error = new ModelApiResponse();
    error.setCode(100);
    error.setType("Validation");
    error.setMessage(ex.getMessage());
    return Mono.just(new ResponseEntity<>(error, HttpStatus.BAD_REQUEST));
  }

  @ExceptionHandler(RuntimeException.class)
  public final Mono<ResponseEntity<ModelApiResponse>> handleRTException(
      RuntimeException ex, ServerWebExchange serverWebExchange) {
    ModelApiResponse error = new ModelApiResponse();
    error.setCode(500);
    error.setType("Internal");
    error.setMessage(ex.getMessage());
    return Mono.just(new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR));
  }
}
