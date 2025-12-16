package com.jonathanmarquez.security.security.controller;

import com.jonathanmarquez.security.exceptions.response.ApiResponseFactory;
import com.jonathanmarquez.security.exceptions.response.SuccessApiResponse;
import com.jonathanmarquez.security.security.model.dto.ForgotPasswordRequestDto;
import com.jonathanmarquez.security.security.model.dto.ResetPasswordRequestDto;
import com.jonathanmarquez.security.security.model.dto.VerifyForgotPasswordOtpRequestDto;
import com.jonathanmarquez.security.security.service.PasswordService;
import com.jonathanmarquez.security.security.utils.CookieUtil;
import com.jonathanmarquez.security.utils.ResponseMessages;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth/password")
@RequiredArgsConstructor
public class PasswordController {

  private final PasswordService passwordService;
  private final ApiResponseFactory apiResponseFactory;
  private final CookieUtil cookieUtil;


  @PostMapping("/forgot")
  public ResponseEntity<SuccessApiResponse<Void>> forgotPasswordRequestOtp(
      @Valid @RequestBody ForgotPasswordRequestDto request
  ) {
    passwordService.requestForgotPasswordOtp(request.email());
    return apiResponseFactory.ok(ResponseMessages.PASSWORD_FORGOT_OTP_SENT_GENERIC);
  }

  @PostMapping("/forgot/verify-otp")
  public ResponseEntity<SuccessApiResponse<Void>> verifyForgotOtp(
      @Valid @RequestBody VerifyForgotPasswordOtpRequestDto request,
      HttpServletResponse response
  ) {
    String resetToken =
        passwordService.verifyForgotPasswordOtpAndIssueResetToken(request.email(), request.otpCode());

    cookieUtil.createPasswordResetTokenCookie(response, resetToken);

    return apiResponseFactory.ok(ResponseMessages.PASSWORD_RESET_TOKEN_ISSUED);
  }

  @PostMapping("/reset")
  public ResponseEntity<SuccessApiResponse<Void>> resetPassword(
      @Valid @RequestBody ResetPasswordRequestDto request,
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse
  ) {
    String resetToken = cookieUtil.getPasswordResetTokenFromCookie(httpServletRequest);

    passwordService.resetPassword(resetToken, request.newPassword(), request.confirmPassword());

    // Consumir el token: borrar cookie (mitigación básica en stateless)
    cookieUtil.deletePasswordResetTokenCookie(httpServletResponse);

    return apiResponseFactory.ok(ResponseMessages.PASSWORD_RESET_SUCCESS);
  }
}