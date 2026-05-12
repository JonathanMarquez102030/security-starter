/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.enums;

/**
 * Contrato que deben cumplir los roles definidos por el consumidor de la librería.
 *
 * <p>Implementa esta interfaz en un enum para definir los roles de tu aplicación.
 * La función {@code name()} es satisfecho automáticamente por el enum.
 * La función {@code getGroupName()} tiene una implementación por defecto que
 * elimina el prefijo {@code ROLE_} del nombre del rol (ej: {@code ROLE_SELLER → "SELLER"}).
 * Puedes sobreescribirlo si necesitas un nombre de grupo diferente.</p>
 *
 * <p>Ejemplo:</p>
 * <pre>
 * public enum AppRole implements RoleDefinition {
 *     ROLE_USER,
 *     ROLE_ADMIN,
 *     ROLE_SELLER;
 *
 *     // getGroupName() auto-deriva: ROLE_SELLER → "SELLER"
 *     // Puedes sobreescribir si necesitas "SELLERS" (plural), etc.
 * }
 * </pre>
 */
public interface RoleDefinition {

    /**
     * Devuelve el nombre del rol tal como está definido en el enum.
     * Spring Security lo almacenará directamente en la tabla {@code authorities}
     * o en {@code group_authorities}.
     * Ej: {@code "ROLE_SELLER"}
     */
    String name();

    /**
     * Devuelve el nombre del grupo para el modo GROUPS de Spring Security JDBC.
     * Por defecto elimina el prefijo {@code ROLE_} del nombre del enum.
     * Ej: {@code ROLE_SELLER → "SELLER"}
     *
     * <p>Sobreescribe esta función si necesitas un nombre personalizado,
     * por ejemplo {@code "SELLERS"} (plural) o un nombre completamente distinto.</p>
     */
    default String getGroupName() {
        return name().replace("ROLE_", "");
    }
}