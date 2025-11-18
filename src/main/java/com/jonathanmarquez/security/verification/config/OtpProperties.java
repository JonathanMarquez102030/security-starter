package com.jonathanmarquez.security.verification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Propiedades configurables para OTP.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "otp")
public class OtpProperties {

  /**
   * Longitud del código OTP.
   */
  private Integer length = 6;

  /**
   * Minutos de validez del OTP.
   */
  private Integer expirationMinutes = 5;

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