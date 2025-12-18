package com.jonathanmarquez.security.security.enums;

/**
 * Enumeración que define los diferentes propósitos para los códigos OTP (One-Time Password).
 *
 * <p>Esta enumeración se utiliza para categorizar y distinguir los diferentes contextos
 * en los que se generan y validan códigos de un solo uso, asegurando que cada código
 * solo pueda utilizarse para su propósito específico y mejorando la seguridad del sistema.</p>
 */
public enum OtpPurpose {
  EMAIL_VERIFICATION,
  PASSWORD_RESET,
  PASSWORD_CHANGE
}