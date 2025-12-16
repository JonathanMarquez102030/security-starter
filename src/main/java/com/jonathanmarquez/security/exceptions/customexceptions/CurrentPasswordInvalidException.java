package com.jonathanmarquez.security.exceptions.customexceptions;

import org.springframework.http.HttpStatus;

public class CurrentPasswordInvalidException extends RuntimeException implements CustomErrorResponse {

  public CurrentPasswordInvalidException() {
    super("La contraseña actual es incorrecta");
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.BAD_REQUEST;
  }
}