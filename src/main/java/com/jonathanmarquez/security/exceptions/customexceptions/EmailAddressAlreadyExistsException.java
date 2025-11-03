package com.jonathanmarquez.security.exceptions.customexceptions;

public class EmailAddressAlreadyExistsException extends RuntimeException {
  private static final String DEFAULT_MESSAGE = "Ya existe una cuenta con ese correo.";

  public EmailAddressAlreadyExistsException() {
    super(DEFAULT_MESSAGE);
  }

  public EmailAddressAlreadyExistsException(String message) {
    super(message);
  }

  public EmailAddressAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }

  public EmailAddressAlreadyExistsException(Throwable cause) {
    super(DEFAULT_MESSAGE, cause);
  }
}
