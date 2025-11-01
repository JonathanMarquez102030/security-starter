package com.jonathanmarquez.security.security.enums;

/**
 * Define el modo de autorización a utilizar en la aplicación.
 */
public enum AuthorizationMode {
    /**
     * Usa la tabla 'authorities' directamente.
     */
    AUTHORITIES,
    
    /**
     * Usa las tablas 'groups', 'group_authorities' y 'group_members'.
     */
    GROUPS
}