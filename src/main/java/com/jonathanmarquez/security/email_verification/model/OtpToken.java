package com.jonathanmarquez.security.email_verification.model;

import com.jonathanmarquez.security.jpa.AuditableEntity;
import com.jonathanmarquez.security.security.enums.OtpPurpose;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para tokens OTP de verificación de email.
 * Hibernate genera automáticamente la tabla 'otp_tokens'.
 */
@Entity
@Table(name = "otp_tokens", indexes = {
    @Index(name = "idx_otp_email", columnList = "email"),
    @Index(name = "idx_otp_code", columnList = "code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpToken extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  /**
   * Email del usuario (FK conceptual a users/user_profiles).
   */
  @Column(name = "email", nullable = false, length = 255)
  private String email;

  /**
   * Código OTP de 6 dígitos.
   */
  @Column(name = "code", nullable = false, length = 6)
  private String code;

  /**
   * Número de intentos de verificación realizados.
   */
  @Column(name = "attempts", nullable = false)
  @Builder.Default
  private Integer attempts = 0;

  /**
   * Fecha y hora de expiración del token.
   */
  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  /**
   * Fecha y hora del último envío de OTP.
   */
  @Column(name = "last_sent_at", nullable = false)
  private LocalDateTime lastSentAt;

  /**
   * Indica si el token ya fue usado exitosamente.
   */
  @Column(name = "verified", nullable = false)
  @Builder.Default
  private Boolean verified = false;

  @Enumerated(EnumType.STRING)
  @Column(name = "purpose", nullable = false, length = 50)
  private OtpPurpose purpose;

  /**
   * Incrementa el contador de intentos.
   */
  public void incrementAttempts() {
    this.attempts++;
  }

  /**
   * Verifica si el OTP ha expirado.
   */
  public boolean isExpired() {
    return LocalDateTime.now().isAfter(this.expiresAt);
  }

  /**
   * Verifica si se alcanzó el máximo de intentos.
   */
  public boolean hasReachedMaxAttempts(int maxAttempts) {
    return this.attempts >= maxAttempts;
  }

  /**
   * Marca el OTP como verificado.
   */
  public void markAsVerified() {
    this.verified = true;
  }
}