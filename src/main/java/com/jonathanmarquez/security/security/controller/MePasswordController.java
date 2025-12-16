package com.jonathanmarquez.security.security.controller;

import com.jonathanmarquez.security.exceptions.customexceptions.UserNotAuthenticatedException;
import com.jonathanmarquez.security.exceptions.response.ApiResponseFactory;
import com.jonathanmarquez.security.exceptions.response.SuccessApiResponse;
import com.jonathanmarquez.security.security.model.dto.ChangePasswordConfirmRequestDto;
import com.jonathanmarquez.security.security.service.PasswordService;
import com.jonathanmarquez.security.security.utils.CookieUtil;
import com.jonathanmarquez.security.utils.ResponseMessages;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me/password")
@RequiredArgsConstructor
public class MePasswordController {

  private final PasswordService passwordService;
  private final ApiResponseFactory apiResponseFactory;
  private final CookieUtil cookieUtil;

  @PostMapping("/change/request-otp")
  public ResponseEntity<SuccessApiResponse<Void>> requestOtp(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new UserNotAuthenticatedException();
    }
    String email = authentication.getName();
    passwordService.requestChangePasswordOtp(email);
    return apiResponseFactory.ok(ResponseMessages.PASSWORD_CHANGE_OTP_SENT);
  }

  @PostMapping("/change/confirm")
  public ResponseEntity<SuccessApiResponse<Void>> confirm(
      Authentication authentication,
      @Valid @RequestBody ChangePasswordConfirmRequestDto request,
      HttpServletResponse response
  ) {
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new UserNotAuthenticatedException();
    }

    String email = authentication.getName();

    passwordService.confirmChangePassword(
        email,
        request.currentPassword(),
        request.newPassword(),
        request.confirmPassword(),
        request.otpCode()
    );

    // Estrategia mínima: borrar cookies y limpiar contexto para forzar re-login.
    cookieUtil.deleteTokenCookies(response);
    SecurityContextHolder.clearContext();

    return apiResponseFactory.ok(ResponseMessages.PASSWORD_CHANGED_SUCCESS);
  }
}