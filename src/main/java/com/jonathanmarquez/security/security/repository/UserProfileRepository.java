package com.jonathanmarquez.security.security.repository;

import com.jonathanmarquez.security.security.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para gestionar los perfiles de usuario extendidos.
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, String> {

  /**
   * Busca un perfil de usuario por email.
   */
  Optional<UserProfile> findByEmail(String email);

  /**
   * Verifica si existe un perfil con el email especificado.
   */
  boolean existsByEmail(String email);
}