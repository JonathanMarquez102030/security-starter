package com.jonathanmarquez.security.exceptions.customexceptions;

import org.springframework.http.HttpStatus;

public class PasswordMismatchException extends RuntimeException implements CustomErrorResponse {

  public PasswordMismatchException() {
    super("Las contraseñas no coinciden");
  }

  public PasswordMismatchException(String message) {
    super(message);
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.BAD_REQUEST;
  }
}