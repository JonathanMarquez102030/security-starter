package com.jonathanmarquez.security.email_verification.service;

/**
 * Servicio para envío de emails.
 */
public interface EmailService {

  /**
   * Envía un código OTP de verificación al email especificado.
   *
   * @param to email del destinatario
   * @param otpCode código OTP a enviar
   * @param expirationMinutes minutos de validez del OTP
   */
  void sendOtpEmail(String to, String otpCode, Integer expirationMinutes);

  /**
   * Envía un email de bienvenida tras verificación exitosa.
   *
   * @param to email del destinatario
   * @param firstName nombre del usuario
   */
  void sendWelcomeEmail(String to, String firstName);
}