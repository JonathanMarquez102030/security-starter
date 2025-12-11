package com.jonathanmarquez.security.email_verification.controller;

import com.jonathanmarquez.security.email_verification.dto.OtpResponseDto;
import com.jonathanmarquez.security.email_verification.dto.ResendOtpRequestDto;
import com.jonathanmarquez.security.email_verification.dto.VerifyOtpRequestDto;
import com.jonathanmarquez.security.email_verification.service.EmailService;
import com.jonathanmarquez.security.email_verification.service.OtpService;
import com.jonathanmarquez.security.exceptions.response.ApiResponseFactory;
import com.jonathanmarquez.security.exceptions.response.SuccessApiResponse;
import com.jonathanmarquez.security.security.repository.UserProfileRepository;
import com.jonathanmarquez.security.security.service.UserProfileService;
import com.jonathanmarquez.security.utils.ResponseMessages;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para verificación de email mediante OTP.
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class VerificationController {

  private final OtpService otpService;
  private final UserProfileService userProfileService;
  private final EmailService emailService;
  private final UserProfileRepository userProfileRepository;
  private final ApiResponseFactory apiResponseFactory;

  /**
   * Verifica un código OTP.
   * <p>
   * POST /api/auth/verify
   * Body: { "email": "user@example.com", "code": "123456" }
   *
   * @param request     verificación request
   * @return respuesta de verificación
   */
  @PostMapping("/verify")
  public ResponseEntity<SuccessApiResponse<Void>> verifyOtp(
      @Valid @RequestBody VerifyOtpRequestDto request) {

    log.info("Solicitud de verificación OTP para email: {}", request.email());

    // Verificar OTP
    otpService.verifyOtp(request.email(), request.code());

    // Habilitar usuario
    userProfileService.setEmailVerified(request.email(), true);

    // Enviar email de bienvenida
    userProfileRepository.findByEmail(request.email()).ifPresent(profile ->
                                                                     emailService.sendWelcomeEmail(request.email(),
                                                                                                   profile.getFirstName())
    );

    log.info("Email verificado exitosamente: {}", request.email());

    return apiResponseFactory.ok(ResponseMessages.EMAIL_VERIFIED);
  }

  /**
   * Reenvía un código OTP.
   * <p>
   * POST /api/auth/resend-otp
   * Body: { "email": "user@example.com" }
   *
   * @param request     reenvío request
   * @return información del OTP reenviado
   */
  @PostMapping("/resend-otp")
  public ResponseEntity<SuccessApiResponse<OtpResponseDto>> resendOtp(
      @Valid @RequestBody ResendOtpRequestDto request) {

    log.info("Solicitud de reenvío OTP para email: {}", request.email());

    OtpResponseDto response = otpService.resendOtp(request.email());

    return apiResponseFactory.ok(ResponseMessages.TOKEN_REFRESHED, response);
  }
}