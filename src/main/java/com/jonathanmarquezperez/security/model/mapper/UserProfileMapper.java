/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.model.mapper;

import com.jonathanmarquezperez.security.model.UserProfile;
import com.jonathanmarquezperez.security.model.dto.UserProfileDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper de MapStruct para conversiones entre entidad UserProfile y su DTO.
 *
 * <p>Esta clase abstracta es procesada por MapStruct en tiempo de compilación para
 * generar automáticamente las implementaciones de los procesos de mapeo entre la entidad
 * de dominio {@link UserProfile} y su objeto de transferencia de datos {@link UserProfileDto}.
 * La configuración componentModel = "spring" permite que MapStruct genere un bean de Spring
 * que puede ser inyectado mediante el contenedor de dependencias.</p>
 */
@Mapper(componentModel = "default")
public abstract class UserProfileMapper {

  /**
   * Convierte una entidad UserProfile a su correspondiente DTO.
   *
   * <p>MapStruct genera automáticamente la implementación de este proceso mapeando
   * todos los campos con nombres coincidentes entre la entidad y el DTO.</p>
   *
   * @param userProfile entidad de dominio a convertir
   * @return DTO con los datos del perfil de usuario
   */
  public abstract UserProfileDto toUserProfileDto(UserProfile userProfile);

  /**
   * Convierte un DTO de UserProfile a su correspondiente entidad de dominio.
   *
   * <p>MapStruct genera automáticamente la implementación de este proceso mapeando
   * todos los campos con nombres coincidentes entre el DTO y la entidad.</p>
   *
   * @param userProfileDto DTO con los datos del perfil de usuario
   * @return entidad de dominio UserProfile
   */
  @Mapping(target = "isEmailVerified", ignore = true)
  public abstract UserProfile toUserProfile(UserProfileDto userProfileDto);
}

