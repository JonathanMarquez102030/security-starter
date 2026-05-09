/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.email_verification.service;

import com.jonathanmarquezperez.security.email_verification.dto.OtpResponseDto;
import com.jonathanmarquezperez.security.security.enums.OtpPurpose;

/**
 * Servicio para gestión de tokens OTP.
 */
public interface OtpService {

  /**
   * Elimina todos los OTP de un usuario.
   *
   * @param email email del usuario
   */
  void deleteAllOtpsByEmail(String email);

  /**
   * Tarea programada para limpiar OTP expirados.
   */
  void cleanupExpiredOtps();

  /**
   * Genera y envía un nuevo código OTP.
   *
   * @param email   email del usuario
   * @param purpose propósito del OTP
   * @return información del OTP generado
   */
  OtpResponseDto generateAndSendOtp(String email, OtpPurpose purpose);

  /**
   * Verifica un código OTP.
   *
   * @param email   email del usuario
   * @param code    código OTP a verificar
   * @param purpose propósito del OTP
   * @return true si la verificación es exitosa
   */
  void verifyOtp(String email, String code, OtpPurpose purpose);

  /**
   * Reenvía un código OTP existente o genera uno nuevo.
   *
   * @param email   email del usuario
   * @param purpose propósito del OTP
   * @return información del OTP reenviado
   */
  OtpResponseDto resendOtp(String email, OtpPurpose purpose);
}
