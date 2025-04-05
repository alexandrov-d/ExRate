package com.example.exrate.controller;

import com.example.exrate.data.ErrorCode;
import com.example.exrate.data.ServiceUnavailableException;
import com.example.exrate.data.rest.ExRateBaseResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@Component
@ControllerAdvice
public class ExRateExceptionHandler {

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ExRateBaseResponse> handleConstraintViolationException(ConstraintViolationException ex) {
    log.error("Request error. {}. Msg: {}", ex.getClass().getSimpleName(), ex.getMessage());
    String msg = ex.getConstraintViolations().stream().map(constraint -> {
      log.error("{}", constraint);
      return constraint.getPropertyPath().toString() + " = " + constraint.getMessage();
    }).collect(Collectors.joining(","));

    ExRateBaseResponse response = new ExRateBaseResponse(ErrorCode.BAD_REQUEST, msg);
    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ExRateBaseResponse> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
    log.error("Request parameter error: {}", ex.getMessage());
    ExRateBaseResponse response = new ExRateBaseResponse(ErrorCode.BAD_REQUEST,
        "Incorrect argument value for '" + ex.getPropertyName() + "' field");
    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(ServletRequestBindingException.class)
  public ResponseEntity<ExRateBaseResponse> handleServletRequestBindingException(ServletRequestBindingException ex) {
    log.error("Validation error. {}", ex.getBody().getDetail());
    ExRateBaseResponse response = new ExRateBaseResponse(ErrorCode.BAD_REQUEST, ex.getBody().getDetail());
    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(ServiceUnavailableException.class)
  public ResponseEntity<ExRateBaseResponse> handleServiceUnavailableException(ServiceUnavailableException ex) {
    log.error("Service Unavailable error. {}", ex.getMessage());
    ExRateBaseResponse response = new ExRateBaseResponse(ErrorCode.SERVICE_UNAVAILABLE, "Service currently unavailable");
    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ExRateBaseResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
    log.error("Illegal argument error.", ex);
    return ResponseEntity.badRequest().body(new ExRateBaseResponse(ErrorCode.BAD_REQUEST, ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ExRateBaseResponse> handleAllExceptions(Exception ex) {
    log.error("Internal error: {}", ex.getClass(), ex);
    return ResponseEntity.internalServerError().body(new ExRateBaseResponse(ErrorCode.INTERNAL_ERROR, "Internal server error"));
  }
}
