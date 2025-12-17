package com.jonathanmarquez.security.security.service.impl;

import com.jonathanmarquez.security.email_verification.exception.InvalidOtpException;
import com.jonathanmarquez.security.email_verification.exception.ResendCooldownException;
import com.jonathanmarquez.security.email_verification.service.OtpService;
import com.jonathanmarquez.security.exceptions.customexceptions.*;
import com.jonathanmarquez.security.security.enums.OtpPurpose;
import com.jonathanmarquez.security.security.service.PasswordService;
import com.jonathanmarquez.security.security.service.UserProfileService;
import com.jonathanmarquez.security.security.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordServiceImpl implements PasswordService {

  private final UserProfileService userProfileService;
  private final OtpService otpService;
  private final JwtUtil jwtUtil;
  private final PasswordEncoder passwordEncoder;
  private final Environment env;

  @Override
  public void requestForgotPasswordOtp(String email) {
    if (!userProfileService.userExists(email)) {
      return;
    }

    try {
      otpService.resendOtp(email, OtpPurpose.PASSWORD_RESET);
    } catch (ResendCooldownException ex) {
      log.debug("Cooldown activo en forgot-password para email (no expuesto).");
    } catch (Exception ex) {
      log.warn("No se pudo enviar OTP de forgot-password (no expuesto).");
    }
  }

  @Override
  public String verifyForgotPasswordOtpAndIssueResetToken(String email, String otpCode) {
    otpService.verifyOtp(email, otpCode, OtpPurpose.PASSWORD_RESET);
    return jwtUtil.generatePasswordResetToken(email);
  }

  @Override
  @Transactional
  public void resetPassword(String resetToken, String newPassword, String confirmPassword) {
    ensurePasswordsMatch(newPassword, confirmPassword);
    ensurePasswordPolicy(newPassword);

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
  }

  @Override
  public void requestChangePasswordOtp(String authenticatedEmail) {
    otpService.resendOtp(authenticatedEmail, OtpPurpose.PASSWORD_CHANGE);
  }

  @Override
  public void confirmChangePassword(
      String authenticatedEmail,
      String currentPassword,
      String newPassword,
      String confirmPassword,
      String otpCode
  ) {
    ensurePasswordsMatch(newPassword, confirmPassword);
    ensurePasswordPolicy(newPassword);

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
  }

  private void ensurePasswordsMatch(String newPassword, String confirmPassword) {
    if (newPassword == null || confirmPassword == null || !newPassword.equals(confirmPassword)) {
      throw new PasswordMismatchException();
    }
  }

  private void ensurePasswordPolicy(String newPassword) {
    int minLen = Integer.parseInt(env.getProperty("password.policy.min-length", "8"));
    String regex = env.getProperty(
        "password.policy.regex",
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$"
    );

    if (newPassword.length() < minLen) {
      throw new PasswordPolicyException("La contraseña debe tener al menos " + minLen + " caracteres");
    }

    Pattern pattern = Pattern.compile(regex);
    if (!pattern.matcher(newPassword).matches()) {
      throw new PasswordPolicyException("La contraseña no cumple la política de complejidad");
    }
  }
}