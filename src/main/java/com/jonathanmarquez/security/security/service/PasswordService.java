package com.jonathanmarquez.security.security.service;


public interface PasswordService {

  void requestForgotPasswordOtp(String email);

  String verifyForgotPasswordOtpAndIssueResetToken(String email, String otpCode);

  void resetPassword(String resetToken, String newPassword, String confirmPassword);

  void requestChangePasswordOtp(String authenticatedEmail);

  void confirmChangePassword(
      String authenticatedEmail,
      String currentPassword,
      String newPassword,
      String confirmPassword,
      String otpCode
  );
}