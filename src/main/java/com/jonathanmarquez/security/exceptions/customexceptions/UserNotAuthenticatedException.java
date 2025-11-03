package com.jonathanmarquez.security.exceptions.customexceptions;

public class UserNotAuthenticatedException extends RuntimeException {
  private static final String DEFAULT_MESSAGE = "Usuario no autenticado.";

  public UserNotAuthenticatedException() {
    super(DEFAULT_MESSAGE);
  }

  public UserNotAuthenticatedException(String message) {
    super(message);
  }

  public UserNotAuthenticatedException(String message, Throwable cause) {
    super(message, cause);
  }

  public UserNotAuthenticatedException(Throwable cause) {
    super(DEFAULT_MESSAGE, cause);
  }
}
