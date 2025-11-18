package com.jonathanmarquez.security.email_verification.service;

import com.jonathanmarquez.security.email_verification.dto.OtpResponseDto;

/**
 * Servicio para gestión de tokens OTP.
 */
public interface OtpService {

  /**
   * Genera y envía un nuevo código OTP.
   *
   * @param email email del usuario
   * @return información del OTP generado
   */
  OtpResponseDto generateAndSendOtp(String email);

  /**
   * Verifica un código OTP.
   *
   * @param email email del usuario
   * @param code código OTP a verificar
   * @return true si la verificación es exitosa
   */
  boolean verifyOtp(String email, String code);

  /**
   * Reenvía un código OTP existente o genera uno nuevo.
   *
   * @param email email del usuario
   * @return información del OTP reenviado
   */
  OtpResponseDto resendOtp(String email);

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
}