package com.jonathanmarquez.security.exceptions.customexceptions;

import org.springframework.http.HttpStatus;

public class PasswordReuseNotAllowedException extends RuntimeException implements CustomErrorResponse {

  public PasswordReuseNotAllowedException() {
    super("La nueva contraseña no puede ser igual a la contraseña actual");
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.BAD_REQUEST;
  }
}