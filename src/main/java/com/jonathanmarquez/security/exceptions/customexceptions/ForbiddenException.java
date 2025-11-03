package com.jonathanmarquez.security.exceptions.customexceptions;

/**
 * Excepción lanzada cuando el usuario no tiene permisos.
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}