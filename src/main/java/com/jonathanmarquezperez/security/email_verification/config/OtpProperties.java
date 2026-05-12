/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.email_verification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades configurables para OTP.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "jmp.otp")
public class OtpProperties {

  /**
   * Minutos de validez del OTP.
   */
  private Integer expirationMinutes = 10;

  /**
   * Máximo de intentos de verificación permitidos.
   */
  private Integer maxAttempts = 5;

  /**
   * Segundos de espera entre reenvíos de OTP.
   */
  private Long resendCooldownSeconds = 60L;

  /**
   * Horas después de las cuales una cuenta no verificada será eliminada.
   */
  private Integer unverifiedAccountExpirationHours = 24;
}
