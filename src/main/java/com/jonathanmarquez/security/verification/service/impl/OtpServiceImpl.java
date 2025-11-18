
package com.jonathanmarquez.security.verification.service.impl;

import com.jonathanmarquez.security.verification.config.OtpProperties;
import com.jonathanmarquez.security.verification.dto.OtpResponseDto;
import com.jonathanmarquez.security.verification.exception.*;
import com.jonathanmarquez.security.verification.model.OtpToken;
import com.jonathanmarquez.security.verification.repository.OtpTokenRepository;
import com.jonathanmarquez.security.verification.service.EmailService;
import com.jonathanmarquez.security.verification.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Implementación del servicio de gestión de OTP.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

  private final OtpTokenRepository otpTokenRepository;
  private final EmailService emailService;
  private final OtpProperties otpProperties;
  private final SecureRandom secureRandom = new SecureRandom();
  private final JdbcTemplate jdbcTemplate;


  @Override
  @Transactional
  public OtpResponseDto generateAndSendOtp(String email) {
    log.info("Generando OTP para email: {}", email);

    // Generar código OTP
    String otpCode = generateOtpCode();
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime expiresAt = now.plusMinutes(otpProperties.getExpirationMinutes());

    // Crear y guardar token
    OtpToken otpToken = OtpToken.builder()
        .email(email)
        .code(otpCode)
        .attempts(0)
        .expiresAt(expiresAt)
        .lastSentAt(now)
        .verified(false)
        .build();

    otpTokenRepository.save(otpToken);
    log.debug("OTP generado y guardado para email: {}", email);

    // Enviar email
    emailService.sendOtpEmail(email, otpCode, otpProperties.getExpirationMinutes());

    return OtpResponseDto.builder()
        .email(email)
        .message("Código OTP enviado exitosamente")
        .expirationMinutes(otpProperties.getExpirationMinutes())
        .build();
  }

  @Override
  @Transactional(noRollbackFor = InvalidOtpException.class)
  public boolean verifyOtp(String email, String code) {
    log.info("Verificando OTP para email: {}", email);

    // Buscar OTP más reciente no verificado
    OtpToken otpToken = otpTokenRepository
        .findTopByEmailAndVerifiedFalseOrderByCreatedDateDesc(email)
        .orElseThrow(OtpNotFoundException::new);

    // Validar si expiró
    if (otpToken.isExpired()) {
      log.warn("OTP expirado para email: {}", email);
      throw new OtpExpiredException();
    }

    // Validar intentos máximos
    if (otpToken.hasReachedMaxAttempts(otpProperties.getMaxAttempts())) {
      log.warn("Máximo de intentos alcanzado para email: {}", email);
      throw new OtpMaxAttemptsException();
    }

    // Incrementar intentos
    otpToken.incrementAttempts();
    otpTokenRepository.saveAndFlush(otpToken);

    // Verificar código
    if (!otpToken.getCode().equals(code)) {
      log.warn("Código OTP inválido para email: {} (intento {}/{})",
          email, otpToken.getAttempts(), otpProperties.getMaxAttempts());
      throw new InvalidOtpException(
          String.format("Código inválido. Intentos restantes: %d",
              otpProperties.getMaxAttempts() - otpToken.getAttempts())
      );
    }

    // Marcar como verificado
    otpToken.setVerified(true);
    otpTokenRepository.save(otpToken);

    log.info("OTP verificado exitosamente para email: {}", email);
    return true;
  }

  @Override
  @Transactional
  public OtpResponseDto resendOtp(String email) {
    log.info("Reenviando OTP para email: {}", email);

    // Buscar OTP más reciente
    OtpToken existingOtp = otpTokenRepository
        .findTopByEmailAndVerifiedFalseOrderByCreatedDateDesc(email)
        .orElse(null);

    // Si existe, validar cooldown
    if (existingOtp != null) {
      LocalDateTime now = LocalDateTime.now();
      long secondsSinceLastSent = ChronoUnit.SECONDS.between(existingOtp.getLastSentAt(), now);

      if (secondsSinceLastSent < otpProperties.getResendCooldownSeconds()) {
        long secondsRemaining = otpProperties.getResendCooldownSeconds() - secondsSinceLastSent;
        log.warn("Intento de reenvío antes del cooldown para email: {} ({} segundos restantes)",
            email, secondsRemaining);
        throw new ResendCooldownException(secondsRemaining);
      }

      // Si no expiró y no alcanzó max intentos, reenviar el mismo código
      if (!existingOtp.isExpired() && 
          !existingOtp.hasReachedMaxAttempts(otpProperties.getMaxAttempts())) {
        
        existingOtp.setLastSentAt(now);
        otpTokenRepository.save(existingOtp);

        emailService.sendOtpEmail(email, existingOtp.getCode(), 
            calculateRemainingMinutes(existingOtp.getExpiresAt()));

        log.info("OTP reenviado (mismo código) para email: {}", email);
        
        return OtpResponseDto.builder()
            .email(email)
            .message("Código OTP reenviado exitosamente")
            .expirationMinutes(calculateRemainingMinutes(existingOtp.getExpiresAt()))
            .build();
      }
    }

    // Si no existe, expiró o alcanzó max intentos, generar nuevo
    log.info("Generando nuevo OTP para email: {}", email);
    return generateAndSendOtp(email);
  }

  @Override
  @Transactional
  public void deleteAllOtpsByEmail(String email) {
    log.info("Eliminando todos los OTP para email: {}", email);
    otpTokenRepository.deleteAllByEmail(email);
  }

  @Override
  @Transactional
  @Scheduled(cron = "0 0 * * * *") // Cada hora
  public void cleanupExpiredOtps() {
    log.info("Ejecutando limpieza de OTP expirados");
    otpTokenRepository.deleteExpiredTokens(LocalDateTime.now());
  }

  /**
   * Genera un código OTP de 6 dígitos numéricos.
   */
  private String generateOtpCode() {
    int otp = secureRandom.nextInt(900000) + 100000; // 100000 a 999999
    return String.valueOf(otp);
  }

  /**
   * Calcula los minutos restantes hasta expiración.
   */
  private Integer calculateRemainingMinutes(LocalDateTime expiresAt) {
    long minutesRemaining = ChronoUnit.MINUTES.between(LocalDateTime.now(), expiresAt);
    return Math.max(0, (int) minutesRemaining);
  }

  /**
   * Tarea programada: Limpia cuentas no verificadas después del tiempo configurado.
   * Se ejecuta cada 6 horas.
   */
  @Scheduled(cron = "0 0 */6 * * *")
  @Transactional
  public void cleanupUnverifiedAccounts() {
    log.info("=== Iniciando limpieza de cuentas no verificadas ===");

    try {
      LocalDateTime threshold = LocalDateTime.now()
                                             .minusHours(otpProperties.getUnverifiedAccountExpirationHours());

      log.debug("Buscando cuentas creadas antes de: {}", threshold);

      // Buscar usuarios no verificados con más del tiempo configurado
      String sql = """
          SELECT u.username, up.created_date
          FROM users u
          INNER JOIN user_profiles up ON u.username = up.email
          WHERE u.enabled = false
            AND up.created_date < ?
          """;

      List<Map<String, Object>> expiredAccounts = jdbcTemplate.queryForList(sql, threshold);

      if (expiredAccounts.isEmpty()) {
        log.info("No hay cuentas no verificadas para eliminar");
        return;
      }

      log.info("Encontradas {} cuentas no verificadas para eliminar", expiredAccounts.size());

      int deletedCount = 0;
      for (Map<String, Object> account : expiredAccounts) {
        String email = (String) account.get("username");
        LocalDateTime createdDate = (LocalDateTime) account.get("created_date");

        try {
          deleteUnverifiedAccount(email);
          deletedCount++;

          long hoursOld = ChronoUnit.HOURS.between(createdDate, LocalDateTime.now());
          log.info("✓ Cuenta eliminada: {} (creada hace {} horas)", email, hoursOld);

        } catch (Exception e) {
          log.error("✗ Error al eliminar cuenta {}: {}", email, e.getMessage());
        }
      }

      log.info("=== Limpieza completada: {}/{} cuentas eliminadas ===",
               deletedCount, expiredAccounts.size());

    } catch (Exception e) {
      log.error("Error crítico en limpieza de cuentas no verificadas", e);
    }
  }

  /**
   * Elimina una cuenta no verificada y todos sus datos relacionados.
   */
  private void deleteUnverifiedAccount(String email) {
    // 1. Eliminar tokens OTP
    otpTokenRepository.deleteAllByEmail(email);

    // 2. Eliminar authorities/group_members
    jdbcTemplate.update("DELETE FROM authorities WHERE username = ?", email);
    jdbcTemplate.update("DELETE FROM group_members WHERE username = ?", email);

    // 3. Eliminar perfil
    jdbcTemplate.update("DELETE FROM user_profiles WHERE email = ?", email);

    // 4. Eliminar usuario
    jdbcTemplate.update("DELETE FROM users WHERE username = ?", email);
  }
}