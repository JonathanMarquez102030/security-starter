package com.jonathanmarquez.security.exceptions.customexceptions;

/**
 * Excepción lanzada cuando falla la autenticación.
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}