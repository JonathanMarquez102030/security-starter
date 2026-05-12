/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.service.impl;

import com.jonathanmarquezperez.email_verification.exception.ResendCooldownException;
import com.jonathanmarquezperez.email_verification.service.OtpService;
import com.jonathanmarquezperez.exceptions.customexceptions.*;
import com.jonathanmarquezperez.security.config.PasswordPolicyValidator;
import com.jonathanmarquezperez.security.enums.OtpPurpose;
import com.jonathanmarquezperez.security.model.dto.ChangePasswordConfirmRequestDto;
import com.jonathanmarquezperez.security.service.PasswordService;
import com.jonathanmarquezperez.security.service.UserProfileService;
import com.jonathanmarquezperez.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {

  private final UserProfileService userProfileService;
  private final OtpService otpService;
  private final JwtUtil jwtUtil;
  private final PasswordEncoder passwordEncoder;
  private final PasswordPolicyValidator passwordPolicyValidator;

  @Override
  public void requestForgotPasswordOtp(String email) {
    log.info("Solicitud de OTP para recuperación de contraseña: {}", email);

    if (!userProfileService.userExists(email)) {
      log.debug("forgot-password: email no existe, respuesta silenciosa");
      return;
    }

    try {
      otpService.resendOtp(email, OtpPurpose.PASSWORD_RESET);
    } catch (ResendCooldownException ex) {
      log.debug("Cooldown activo en forgot-password para email (no expuesto).");
      throw ex;
    } catch (Exception ex) {
      log.warn("No se pudo enviar OTP de forgot-password (no expuesto).");
    }
  }

  @Override
  public String verifyForgotPasswordOtpAndIssueResetToken(String email, String otpCode) {
    log.info("Verificando OTP de recuperación de contraseña para: {}", email);
    otpService.verifyOtp(email, otpCode, OtpPurpose.PASSWORD_RESET);
    String resetToken = jwtUtil.generatePasswordResetToken(email);
    log.debug("Reset token generado para: {}", email);
    return resetToken;
  }

  @Override
  @Transactional
  public void resetPassword(String resetToken, String newPassword, String confirmPassword) {
    log.debug("Procesando reset de contraseña");

    ensurePasswordsMatch(newPassword, confirmPassword);
    passwordPolicyValidator.validate(newPassword);

    if (resetToken == null || resetToken.isBlank() || !jwtUtil.isPasswordResetTokenValid(resetToken)) {
      throw new InvalidPasswordResetTokenException();
    }

    String email = jwtUtil.extractUsername(resetToken);
    if (email == null || email.isBlank()) {
      throw new InvalidPasswordResetTokenException();
    }

    if (!userProfileService.userExists(email)) {
      throw new InvalidPasswordResetTokenException();
    }

    String currentHash = userProfileService.getPasswordHash(email);
    if (currentHash != null && passwordEncoder.matches(newPassword, currentHash)) {
      throw new PasswordReuseNotAllowedException();
    }

    userProfileService.updatePassword(email, newPassword);
    otpService.deleteAllOtpsByEmail(email);
    log.info("Contraseña reseteada exitosamente para: {}", email);
  }

  @Override
  public void requestChangePasswordOtp(String authenticatedEmail) {
    log.info("Solicitud de OTP para cambio de contraseña: {}", authenticatedEmail);
    otpService.resendOtp(authenticatedEmail, OtpPurpose.PASSWORD_CHANGE);
  }

  @Override
  public void confirmChangePassword(String authenticatedEmail,
                                    ChangePasswordConfirmRequestDto passwordChangeDto
  ) {
    log.info("Confirmando cambio de contraseña para: {}", authenticatedEmail);

    String newPassword = passwordChangeDto.newPassword();
    String confirmPassword = passwordChangeDto.confirmPassword();
    String currentPassword = passwordChangeDto.currentPassword();
    String otpCode = passwordChangeDto.otpCode();

    ensurePasswordsMatch(newPassword, confirmPassword);
    passwordPolicyValidator.validate(newPassword);

    String currentHash = userProfileService.getPasswordHash(authenticatedEmail);
    if (currentHash == null || currentHash.isBlank()) {
      throw new CurrentPasswordInvalidException();
    }

    if (!passwordEncoder.matches(currentPassword, currentHash)) {
      throw new CurrentPasswordInvalidException();
    }

    if (passwordEncoder.matches(newPassword, currentHash)) {
      throw new PasswordReuseNotAllowedException();
    }

    otpService.verifyOtp(authenticatedEmail, otpCode, OtpPurpose.PASSWORD_CHANGE);

    userProfileService.updatePassword(authenticatedEmail, newPassword);
    otpService.deleteAllOtpsByEmail(authenticatedEmail);

    log.info("Contraseña cambiada exitosamente para: {}", authenticatedEmail);
  }

  private void ensurePasswordsMatch(String newPassword, String confirmPassword) {
    if (newPassword == null || !newPassword.equals(confirmPassword)) {
      throw new PasswordMismatchException();
    }
  }
}
