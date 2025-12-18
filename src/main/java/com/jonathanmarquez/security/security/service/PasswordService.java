package com.jonathanmarquez.security.security.service;


import com.jonathanmarquez.security.security.model.dto.ChangePasswordConfirmRequestDto;

public interface PasswordService {

  void requestForgotPasswordOtp(String email);

  String verifyForgotPasswordOtpAndIssueResetToken(String email, String otpCode);

  void resetPassword(String resetToken, String newPassword, String confirmPassword);

  void requestChangePasswordOtp(String authenticatedEmail);

  void confirmChangePassword(
      String authenticatedEmail,
      ChangePasswordConfirmRequestDto changePasswordConfirmRequestDto
  );
}