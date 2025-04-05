package com.example.exrate.data;

/** Thrown when external service unavailable or doesn't have any data */
public class ServiceUnavailableException extends RuntimeException {
  public ServiceUnavailableException(String msg) {
    super(msg);
  }

  public ServiceUnavailableException(String msg, Throwable cause) {
    super(msg, cause);
  }
}
