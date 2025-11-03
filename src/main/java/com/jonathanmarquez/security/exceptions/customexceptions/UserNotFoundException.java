package com.jonathanmarquez.security.exceptions.customexceptions;

/**
 * Excepción que indica que un producto no ha sido encontrado.
 *
 * <p>Esta excepción se lanza cuando se intenta acceder a un producto que no existe en la base de
 * datos.
 */
public class UserNotFoundException extends RuntimeException {


  private static final String DEFAULT_MESSAGE = "Producto no encontrado.";

  public UserNotFoundException() {
    super(DEFAULT_MESSAGE);
  }

  public UserNotFoundException(String message) {
    super(message);
  }

  public UserNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public UserNotFoundException(Throwable cause) {
    super(DEFAULT_MESSAGE, cause);
  }
}
