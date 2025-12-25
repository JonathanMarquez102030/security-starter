package com.jonathanmarquez.security.security.controller;

import com.jonathanmarquez.security.email_verification.exception.InvalidOtpException;
import com.jonathanmarquez.security.exceptions.customexceptions.InvalidPasswordResetTokenException;
import com.jonathanmarquez.security.exceptions.customexceptions.PasswordMismatchException;
import com.jonathanmarquez.security.exceptions.customexceptions.PasswordPolicyException;
import com.jonathanmarquez.security.exceptions.customexceptions.PasswordReuseNotAllowedException;
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


/**
 * Controlador REST para gestionar el proceso de recuperación de contraseña olvidada.
 *
 * <p>Este controlador proporciona endpoints públicos que permiten a los usuarios
 * restablecer su contraseña mediante un flujo seguro de tres pasos con verificación
 * OTP y token JWT. No requiere autenticación previa, ya que está diseñado para
 * usuarios que no pueden acceder a sus cuentas.</p>
 */
@Slf4j
@RestController
@RequestMapping("/auth/password")
@RequiredArgsConstructor
public class PasswordController {

  private final PasswordService passwordService;
  private final ApiResponseFactory apiResponseFactory;
  private final CookieUtil cookieUtil;


  /**
   * Solicita el envío de un código OTP para iniciar el proceso de recuperación de contraseña.
   *
   * <p>Envía un código de verificación de un solo uso al correo electrónico proporcionado si
   * la cuenta existe. Por razones de seguridad, siempre retorna una respuesta exitosa genérica
   * sin revelar si el correo está registrado o no, previniendo la enumeración de usuarios.</p>
   *
   * @param request datos de la solicitud conteniendo el correo electrónico del usuario
   * @return respuesta exitosa genérica indicando que el proceso ha sido iniciado
   */
  @PostMapping("/forgot")
  public ResponseEntity<SuccessApiResponse<Void>> forgotPasswordRequestOtp(
      @Valid @RequestBody ForgotPasswordRequestDto request
  ) {
    passwordService.requestForgotPasswordOtp(request.email());
    return apiResponseFactory.ok(ResponseMessages.PASSWORD_FORGOT_OTP_SENT_GENERIC);
  }

  /**
   * Verifica el código OTP proporcionado y emite un token de restablecimiento de contraseña.
   *
   * <p>Valida que el código OTP sea correcto y no haya expirado. En caso exitoso, genera
   * un token JWT de restablecimiento con tiempo de vida limitado y lo almacena en una
   * cookie HTTP-only segura para su uso en el paso final del proceso.</p>
   *
   * @param request  datos de la solicitud conteniendo el correo electrónico y código OTP
   * @param response objeto de respuesta HTTP donde se establecerá la cookie con el token
   * @return respuesta exitosa indicando que el token ha sido emitido
   * @throws InvalidOtpException si el código OTP es inválido, expirado o no existe
   */
  @PostMapping("/forgot/verify-otp")
  public ResponseEntity<SuccessApiResponse<Void>> verifyForgotOtp(
      @Valid @RequestBody VerifyForgotPasswordOtpRequestDto request,
      HttpServletResponse response
  ) {
    String resetToken =
        passwordService.verifyForgotPasswordOtpAndIssueResetToken(request.email(), request.otpCode());

    cookieUtil.createPasswordResetTokenCookie(response, resetToken);

    log.info("Token de reestablecimiento de contraseña emitido con éxito para el usuario: {}", request.email());
    return apiResponseFactory.ok(ResponseMessages.PASSWORD_RESET_TOKEN_ISSUED);
  }

  /**
   * Restablece la contraseña del usuario utilizando el token previamente emitido.
   *
   * <p>Extrae el token de restablecimiento de la cookie, valida que sea válido y no haya expirado,
   * verifica que las contraseñas coincidan y cumplan las políticas de seguridad, y actualiza
   * la contraseña del usuario. Tras el restablecimiento exitoso, elimina la cookie del token
   * y todos los códigos OTP asociados al usuario.</p>
   *
   * @param request             datos de la solicitud conteniendo la nueva contraseña y su confirmación
   * @param httpServletRequest  objeto de solicitud HTTP del cual se extraerá la cookie con el token
   * @param httpServletResponse objeto de respuesta HTTP donde se eliminará la cookie del token
   * @return respuesta exitosa indicando que la contraseña ha sido restablecida
   * @throws InvalidPasswordResetTokenException si el token es inválido, expirado o no existe
   * @throws PasswordMismatchException          si las contraseñas no coinciden
   * @throws PasswordPolicyException            si la contraseña no cumple con las políticas de seguridad
   * @throws PasswordReuseNotAllowedException   si la nueva contraseña es igual a la anterior
   */
  @PostMapping("/reset")
  public ResponseEntity<SuccessApiResponse<Void>> resetPassword(
      @Valid @RequestBody ResetPasswordRequestDto request,
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse
  ) {
    String resetToken = cookieUtil.getPasswordResetTokenFromCookie(httpServletRequest);

    passwordService.resetPassword(resetToken, request.newPassword(), request.confirmPassword());
    cookieUtil.deletePasswordResetTokenCookie(httpServletResponse);

    log.info("Contraseña restablecida exitosamente para el usuario: {}", httpServletRequest.getRemoteUser());

    return apiResponseFactory.ok(ResponseMessages.PASSWORD_RESET_SUCCESS);
  }
}