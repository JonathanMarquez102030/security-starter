/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.security.config;

import com.jonathanmarquezperez.security.security.enums.RoleDefinition;

import java.util.List;

/**
 * Proveedor de roles por defecto asignados durante el registro de un nuevo usuario.
 *
 * <p>La librería registra un bean con {@code @ConditionalOnMissingBean} que retorna
 * {@code Role.ROLE_USER}. El consumidor puede sobreescribirlo declarando su propio bean:</p>
 *
 * <pre>
 * {@code
 * @Bean
 * public DefaultRoleProvider defaultRoleProvider() {
 *     return () -> List.of(AppRole.ROLE_CLIENT);
 * }
 * }
 * </pre>
 */
@FunctionalInterface
public interface DefaultRoleProvider {

    /**
     * Retorna la lista de roles que se asignarán automáticamente al registrar un usuario.
     */
    List<? extends RoleDefinition> getDefaultRegistrationRoles();
}