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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para gestionar operaciones de cambio de contraseña de usuarios autenticados.
 *
 * <p>Este controlador proporciona endpoints protegidos que permiten a usuarios autenticados
 * cambiar su contraseña mediante un flujo de dos pasos con verificación OTP. Todos los
 * endpoints requieren autenticación previa y operan sobre el usuario actualmente autenticado.</p>
 *
 * <p>Tras un cambio exitoso de contraseña, se invalidan las sesiones activas para
 * requerir nueva autenticación con las credenciales actualizadas.</p>
 */
@RestController
@RequestMapping("/me/password")
@RequiredArgsConstructor
public class MePasswordController {

  private final PasswordService passwordService;
  private final ApiResponseFactory apiResponseFactory;
  private final CookieUtil cookieUtil;


  /**
   * Solicita el envío de un código OTP al correo del usuario autenticado para cambiar contraseña.
   *
   * <p>Este endpoint inicia el proceso de cambio de contraseña enviando un código de verificación
   * de un solo uso al correo electrónico asociado a la cuenta del usuario autenticado. El código
   * será requerido posteriormente para confirmar el cambio.</p>
   *
   * @param authentication objeto de autenticación de Spring Security con información del usuario actual
   * @return respuesta exitosa indicando que el OTP ha sido enviado
   * @throws UserNotAuthenticatedException si el usuario no está autenticado o la autenticación es nula
   */
  @PreAuthorize("isAuthenticated()")
  @PostMapping("/change/request-otp")
  public ResponseEntity<SuccessApiResponse<Void>> requestOtp(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new UserNotAuthenticatedException();
    }
    String email = authentication.getName();
    passwordService.requestChangePasswordOtp(email);
    return apiResponseFactory.ok(ResponseMessages.PASSWORD_CHANGE_OTP_SENT);
  }

  /**
   * Confirma y ejecuta el cambio de contraseña tras validar la contraseña actual y el código OTP.
   *
   * <p>Procesa la solicitud de cambio de contraseña validando que el usuario proporcione su contraseña
   * actual correcta, que las nuevas contraseñas coincidan y cumplan políticas de seguridad, y que
   * el código OTP sea válido. Tras un cambio exitoso, se invalidan todas las sesiones activas
   * (eliminando cookies de autenticación y limpiando el contexto de seguridad) para forzar
   * una nueva autenticación con las credenciales actualizadas.</p>
   *
   * @param authentication objeto de autenticación de Spring Security con información del usuario actual
   * @param request        datos de la solicitud conteniendo contraseña actual, nueva contraseña, confirmación y código OTP
   * @param response       objeto de respuesta HTTP donde se eliminarán las cookies de autenticación
   * @return respuesta exitosa indicando que la contraseña ha sido cambiada
   * @throws UserNotAuthenticatedException si el usuario no está autenticado o la autenticación es nula
   */
  @PreAuthorize("isAuthenticated()")
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

    passwordService.confirmChangePassword(email, request);

    // Borrar cookies y limpiar contexto para forzar re-login.
    cookieUtil.deleteTokenCookies(response);
    SecurityContextHolder.clearContext();

    return apiResponseFactory.ok(ResponseMessages.PASSWORD_CHANGED_SUCCESS);
  }
}