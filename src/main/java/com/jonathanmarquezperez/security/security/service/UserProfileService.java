/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.security.service;

import com.jonathanmarquezperez.security.security.enums.RoleDefinition;
import com.jonathanmarquezperez.security.security.model.UserProfile;
import com.jonathanmarquezperez.security.security.model.dto.RegisterRequestDto;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

/**
 * Interfaz de servicio para gestionar perfiles de usuario y sus credenciales.
 *
 * <p>Define los contratos para operaciones CRUD sobre perfiles de usuario, gestión de
 * contraseñas, estados de cuenta (habilitado/verificado) y carga de detalles de usuario
 * para el proceso de autenticación de Spring Security.</p>
 */
public interface UserProfileService {

  /**
   * Crea un nuevo usuario en el sistema con los roles especificados.
   *
   * @param registerRequestDto datos de registro del usuario incluyendo email, contraseña y datos del perfil
   * @param roles              lista de roles a asignar al nuevo usuario
   * @return perfil del usuario recién creado
   */
  UserProfile createUser(RegisterRequestDto registerRequestDto, List<? extends RoleDefinition> roles);

  /**
   * Obtiene los detalles del usuario para autenticación de Spring Security.
   *
   * @param email dirección de correo electrónico del usuario
   * @return objeto UserDetails con información del usuario y sus autoridades
   */
  UserDetails getUserDetails(String email);

  /**
   * Actualiza el perfil de un usuario existente.
   *
   * @param profile perfil de usuario con los datos actualizados
   * @return perfil de usuario actualizado
   */
  UserProfile updateProfile(UserProfile profile);

  /**
   * Actualiza la contraseña de un usuario aplicando cifrado.
   *
   * @param email          dirección de correo electrónico del usuario
   * @param newRawPassword nueva contraseña en texto plano que será cifrada
   */
  void updatePassword(String email, String newRawPassword);

  /**
   * Obtiene el hash cifrado de la contraseña actual del usuario.
   *
   * @param email dirección de correo electrónico del usuario
   * @return hash cifrado de la contraseña
   */
  String getPasswordHash(String email);

  /**
   * Elimina un usuario del sistema.
   *
   * @param email dirección de correo electrónico del usuario a eliminar
   */
  void deleteUser(String email);

  /**
   * Habilita o deshabilita la cuenta de un usuario.
   *
   * @param email   dirección de correo electrónico del usuario
   * @param enabled true para habilitar la cuenta, false para deshabilitarla
   */
  void setEnabled(String email, boolean enabled);

  /**
   * Verifica si existe un usuario con el correo electrónico especificado.
   *
   * @param email dirección de correo electrónico a verificar
   * @return true si existe un usuario con ese email, false en caso contrario
   */
  boolean userExists(String email);

  /**
   * Marca el correo electrónico del usuario como verificado o no verificado.
   *
   * @param email    dirección de correo electrónico del usuario
   * @param verified true para marcar como verificado, false para marcar como no verificado
   */
  void setEmailVerified(String email, boolean verified);
}
