package com.jonathanmarquez.security.email_verification.service;

import com.jonathanmarquez.security.security.enums.OtpPurpose;

/**
 * Servicio para envío de emails.
 */
public interface EmailService {

  /**
   * Envía un código OTP de verificación al email especificado.
   *
   * @param toEmail           email del destinatario
   * @param otpCode           código OTP a enviar
   * @param expirationMinutes minutos de validez del OTP
   * @param purpose           propósito del OTP
   */
  void sendOtpEmail(String toEmail, String otpCode, Integer expirationMinutes, OtpPurpose purpose);

  /**
   * Envía un email de bienvenida tras verificación exitosa.
   *
   * @param to        email del destinatario
   * @param firstName nombre del usuario
   */
  void sendWelcomeEmail(String to, String firstName);
}