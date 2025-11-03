package com.jonathanmarquez.security.exceptions.customexceptions;

/**
 * Excepción lanzada cuando hay un conflicto (ej: datos duplicados).
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}