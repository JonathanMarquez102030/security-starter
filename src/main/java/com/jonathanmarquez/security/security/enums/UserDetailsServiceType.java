package com.jonathanmarquez.security.security.enums;

/**
 * Define el tipo de UserDetailsService a utilizar.
 */
public enum UserDetailsServiceType {
    /**
     * Usa JdbcUserDetailsManager (solo tablas estándar de Spring Security).
     */
    JDBC,
    
    /**
     * Usa CustomUserDetailsService (tablas Spring Security + perfil extendido).
     */
    CUSTOM
}