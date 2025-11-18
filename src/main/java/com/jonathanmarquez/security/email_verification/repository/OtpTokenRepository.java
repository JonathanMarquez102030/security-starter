package com.jonathanmarquez.security.email_verification.repository;

import com.jonathanmarquez.security.email_verification.model.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para gestionar tokens OTP.
 */
@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, UUID> {

  /**
   * Busca el OTP más reciente y no verificado para un email.
   */
  Optional<OtpToken> findTopByEmailAndVerifiedFalseOrderByCreatedDateDesc(String email);

  /**
   * Busca un OTP por email y código que no haya sido verificado.
   */
  Optional<OtpToken> findByEmailAndCodeAndVerifiedFalse(String email, String code);

  /**
   * Obtiene todos los OTP de un email (para limpieza tras verificación exitosa).
   */
  List<OtpToken> findAllByEmail(String email);

  /**
   * Elimina todos los OTP de un email.
   */
  @Modifying
  @Query("DELETE FROM OtpToken o WHERE o.email = :email")
  void deleteAllByEmail(@Param("email") String email);

  /**
   * Elimina OTP expirados (tarea de limpieza).
   */
  @Modifying
  @Query("DELETE FROM OtpToken o WHERE o.expiresAt < :now")
  void deleteExpiredTokens(@Param("now") LocalDateTime now);

  /**
   * Verifica si existe un OTP no verificado para un email.
   */
  boolean existsByEmailAndVerifiedFalse(String email);
}