/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.security.repository;

import com.jonathanmarquezperez.security.security.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para gestionar los perfiles de usuario en la base de datos.
 *
 * <p>Esta interfaz proporciona operaciones de acceso a datos para la entidad {@link UserProfile},
 * extendiendo las capacidades básicas de JpaRepository con procesos de consulta personalizados
 * para búsqueda y verificación de perfiles por dirección de correo electrónico.</p>
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {

  /**
   * Busca un perfil de usuario por su dirección de correo electrónico.
   *
   * @param email dirección de correo electrónico del usuario a buscar
   * @return Optional conteniendo el perfil si existe, Optional.empty() si no se encuentra
   */
  Optional<UserProfile> findByEmail(String email);

  /**
   * Verifica si existe un perfil de usuario con la dirección de correo electrónico especificada.
   *
   * @param email dirección de correo electrónico a verificar
   * @return true si existe un perfil con ese email, false en caso contrario
   */
  boolean existsByEmail(String email);
}
