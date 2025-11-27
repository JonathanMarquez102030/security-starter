package com.jonathanmarquez.security.security.controller;

import com.jonathanmarquez.security.email_verification.service.OtpService;
import com.jonathanmarquez.security.exceptions.customexceptions.EmailAddressAlreadyExistsException;
import com.jonathanmarquez.security.exceptions.customexceptions.InvalidRefreshTokenException;
import com.jonathanmarquez.security.exceptions.customexceptions.UserNotAuthenticatedException;
import com.jonathanmarquez.security.exceptions.response.SuccessApiResponse;
import com.jonathanmarquez.security.security.enums.Role;
import com.jonathanmarquez.security.security.model.SecurityUserDetails;
import com.jonathanmarquez.security.security.model.UserProfile;
import com.jonathanmarquez.security.security.model.dto.AuthResponseDto;
import com.jonathanmarquez.security.security.model.dto.RegisterRequestDto;
import com.jonathanmarquez.security.security.model.dto.UserProfileDto;
import com.jonathanmarquez.security.security.model.mapper.UserProfileMapper;
import com.jonathanmarquez.security.security.service.UserProfileService;
import com.jonathanmarquez.security.security.utils.CookieUtil;
import com.jonathanmarquez.security.security.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST para la gestión de autenticación y autorización de usuarios.
 *
 * <p>Este controlador implementa un sistema de autenticación basado en JWT (JSON Web Tokens)
 * con almacenamiento seguro en cookies HttpOnly. Proporciona endpoints para login, registro,
 * renovación de tokens, obtención del usuario actual y logout.</p>
 *
 * <p>Flujo de autenticación:</p>
 * <ol>
 *   <li>Cliente envía credenciales vía Basic Auth a /api/auth/login</li>
 *   <li>BasicAuthenticationFilter valida las credenciales</li>
 *   <li>JWTTokenGeneratorFilter genera tokens JWT (access y refresh)</li>
 *   <li>Tokens se almacenan en cookies seguras HttpOnly</li>
 *   <li>Requests subsiguientes usan JWT desde cookies (validados por JWTTokenValidatorFilter)</li>
 * </ol>
 *
 * @author Jonathan Marquez
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserProfileService userProfileService;
  private final JwtUtil jwtUtil;
  private final CookieUtil cookieUtil;
  private final UserProfileMapper userProfileMapper;
  private final OtpService otpService; // AGREGAR ESTA LÍNEA

  /**
   * Autentica a un usuario mediante credenciales Basic Auth.
   *
   * <p>Spring Security maneja la autenticación a través del BasicAuthenticationFilter.
   * El JWTTokenGeneratorFilter genera automáticamente los tokens JWT que se almacenan
   * en cookies HttpOnly y Secure (en producción) con política SameSite=Strict.</p>
   *
   * <p>Las cookies generadas son:</p>
   * <ul>
   *   <li>accessToken: Token de corta duración para acceso a recursos</li>
   *   <li>refreshToken: Token de larga duración para renovación</li>
   * </ul>
   *
   * @param authentication objeto de autenticación proporcionado por Spring Security
   * @param request        solicitud HTTP para obtener información de contexto
   * @return ResponseEntity con datos del usuario autenticado en formato estandarizado
   * @throws UserNotAuthenticatedException si el usuario no está autenticado
   */
  @GetMapping("/login")
  public ResponseEntity<SuccessApiResponse<AuthResponseDto>> login(
      Authentication authentication,
      HttpServletRequest request) {

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new UserNotAuthenticatedException();
    }

    SecurityUserDetails user = (SecurityUserDetails) authentication.getPrincipal();
    log.info("Login exitoso para usuario: {}", user.getUsername());

    AuthResponseDto authData = buildAuthResponse(user);

    SuccessApiResponse<AuthResponseDto> response =
        SuccessApiResponse.<AuthResponseDto>builder()
                          .success(true)
                          .message("Login exitoso")
                          .status(HttpStatus.OK.getReasonPhrase())
                          .statusCode(HttpStatus.OK.value())
                          .timestamp(LocalDateTime.now())
                          .path(request.getRequestURI())
                          .data(authData)
                          .build();

    return ResponseEntity.ok(response);
  }

  /**
   * Registra un nuevo usuario en el sistema.
   *
   * <p>Valida que el username no esté duplicado, crea el usuario con la contraseña
   * encriptada y asigna el rol ROLE_USER por defecto. Adicionalmente, registra
   * información de perfil como nombre, apellido y teléfono.</p>
   *
   * @param registerRequest datos de registro del nuevo usuario validados
   * @param request         solicitud HTTP para obtener información de contexto
   * @return ResponseEntity con datos del usuario registrado en formato estandarizado
   * @throws EmailAddressAlreadyExistsException si el username ya existe en el sistema
   */
  @PostMapping("/register")
  public ResponseEntity<SuccessApiResponse<UserProfileDto>> register(
      @Valid @RequestBody RegisterRequestDto registerRequest,
      HttpServletRequest request) {

    if (userProfileService.userExists(registerRequest.email())) {
      throw new EmailAddressAlreadyExistsException(
          "El correo '" + registerRequest.email() + "' ya está registrado"
      );
    }

    UserProfile userProfile = userProfileService.createUser(registerRequest, List.of(Role.ROLE_USER));

    // 2. Enviar OTP en operación separada (no afecta transacción principal)
    try {
      otpService.generateAndSendOtp(registerRequest.email());
      log.info("OTP generado y enviado para: {}", registerRequest.email());
    } catch (Exception e) {
      log.error("Error al generar/enviar OTP para {}: {}", registerRequest.email(), e.getMessage(), e);
      // El usuario ya está creado, solo falló el envío del email
      // Se puede reenviar después
    }

    UserProfileDto userProfileDto = userProfileMapper.toUserProfileDto(userProfile);

    log.info("Usuario registrado exitosamente: {}", registerRequest.email());

    SuccessApiResponse<UserProfileDto> response =
        SuccessApiResponse.<UserProfileDto>builder()
                          .success(true)
                          .message("Usuario registrado exitosamente")
                          .status(HttpStatus.CREATED.getReasonPhrase())
                          .statusCode(HttpStatus.CREATED.value())
                          .timestamp(LocalDateTime.now())
                          .path(request.getRequestURI())
                          .data(userProfileDto)
                          .build();

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * Obtiene información del usuario actualmente autenticado.
   *
   * <p>Recupera los datos del usuario desde el contexto de seguridad. Si la autenticación
   * es vía JWT, carga los datos completos del usuario desde la base de datos. Si es vía
   * Basic Auth, utiliza los datos ya cargados en el objeto principal.</p>
   *
   * @param authentication objeto de autenticación del usuario actual
   * @param request        solicitud HTTP para obtener información de contexto
   * @return ResponseEntity con datos completos del usuario en formato estandarizado
   * @throws UserNotAuthenticatedException si el usuario no está autenticado
   */
  @GetMapping("/me")
  public ResponseEntity<SuccessApiResponse<AuthResponseDto>> getCurrentUser(
      Authentication authentication,
      HttpServletRequest request) {

    if (authentication == null || !authentication.isAuthenticated()) {
      throw new UserNotAuthenticatedException();
    }

    SecurityUserDetails user;

    if (authentication.getPrincipal() instanceof SecurityUserDetails) {
      user = (SecurityUserDetails) authentication.getPrincipal();
    } else {
      String username = authentication.getName();
      user = (SecurityUserDetails) userProfileService.getUserDetails(username);
    }

    AuthResponseDto authData = buildAuthResponse(user);

    SuccessApiResponse<AuthResponseDto> response =
        SuccessApiResponse.<AuthResponseDto>builder()
                          .success(true)
                          .message("Usuario obtenido exitosamente")
                          .status(HttpStatus.OK.getReasonPhrase())
                          .statusCode(HttpStatus.OK.value())
                          .timestamp(LocalDateTime.now())
                          .path(request.getRequestURI())
                          .data(authData)
                          .build();

    return ResponseEntity.ok(response);
  }

  /**
   * Renueva el access token utilizando el refresh token.
   *
   * <p>Extrae el refresh token de las cookies, valida su autenticidad y vigencia,
   * y genera un nuevo access token con los mismos privilegios. El nuevo token se
   * almacena en una cookie HttpOnly reemplazando el anterior.</p>
   *
   * @param request  solicitud HTTP que contiene el refresh token en cookies
   * @param response respuesta HTTP donde se establecerá la nueva cookie
   * @return ResponseEntity con confirmación de renovación en formato estandarizado
   * @throws InvalidRefreshTokenException si el refresh token es inválido o ha expirado
   */
  @PostMapping("/refresh")
  public ResponseEntity<SuccessApiResponse<Void>> refreshToken(
      HttpServletRequest request,
      HttpServletResponse response) {

    String refreshToken = cookieUtil.getRefreshTokenFromCookie(request);

    if (refreshToken == null || !jwtUtil.isRefreshTokenValid(refreshToken)) {
      throw new InvalidRefreshTokenException();
    }

    String username = jwtUtil.extractUsername(refreshToken);
    String authorities = jwtUtil.extractAuthorities(refreshToken);

    Authentication auth = new UsernamePasswordAuthenticationToken(
        username,
        null,
        AuthorityUtils.commaSeparatedStringToAuthorityList(
            authorities != null && !authorities.equals("null") ? authorities : ""
        )
    );

    String newAccessToken = jwtUtil.generateAccessToken(auth);
    cookieUtil.createAccessTokenCookie(response, newAccessToken);

    log.info("Token renovado exitosamente para usuario: {}", username);

    SuccessApiResponse<Void> apiResponse =
        SuccessApiResponse.<Void>builder()
                          .success(true)
                          .message("Token renovado exitosamente")
                          .status(HttpStatus.OK.getReasonPhrase())
                          .statusCode(HttpStatus.OK.value())
                          .timestamp(LocalDateTime.now())
                          .path(request.getRequestURI())
                          .build();

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * Cierra la sesión del usuario actual.
   *
   * <p>Elimina las cookies que contienen los tokens JWT (accessToken y refreshToken)
   * y limpia el contexto de seguridad de Spring Security. Esto invalida la sesión
   * del usuario en el cliente.</p>
   *
   * @param request  solicitud HTTP para obtener información de contexto
   * @param response respuesta HTTP donde se eliminarán las cookies
   * @return ResponseEntity con confirmación de logout en formato estandarizado
   */
  @PostMapping("/logout")
  public ResponseEntity<SuccessApiResponse<Void>> logout(
      HttpServletRequest request,
      HttpServletResponse response) {

    cookieUtil.deleteTokenCookies(response);
    SecurityContextHolder.clearContext();

    log.info("Logout ejecutado exitosamente");

    SuccessApiResponse<Void> apiResponse =
        SuccessApiResponse.<Void>builder()
                          .success(true)
                          .message("Logout exitoso")
                          .status(HttpStatus.OK.getReasonPhrase())
                          .statusCode(HttpStatus.OK.value())
                          .timestamp(LocalDateTime.now())
                          .path(request.getRequestURI())
                          .build();

    return ResponseEntity.ok(apiResponse);
  }

  /**
   * Endpoint para obtener el token CSRF.
   *
   * <p>Este endpoint permite a los clientes obtener el token CSRF necesario para
   * realizar operaciones que modifiquen estado. El token es generado y manejado
   * automáticamente por Spring Security.</p>
   *
   * @return ResponseEntity vacío con código 200 OK
   */
  @GetMapping("/csrf")
  public ResponseEntity<Void> getCsrfToken() {
    return ResponseEntity.ok().build();
  }

  // ===================================================================================================================
  // Métodos Auxiliares
  // ===================================================================================================================

  /**
   * Construye el objeto de respuesta de autenticación con los datos del usuario.
   *
   * <p>Extrae la información relevante del usuario autenticado incluyendo datos
   * de perfil, autoridades y estado de habilitación para construir un DTO
   * estandarizado de respuesta.</p>
   *
   * @param user detalles del usuario autenticado
   * @return AuthResponseDto con la información del usuario estructurada
   */
  private AuthResponseDto buildAuthResponse(SecurityUserDetails user) {

    UserProfileDto profile = userProfileMapper.toUserProfileDto(user.getProfile());
    return AuthResponseDto.builder()
                          .profile(profile)
                          .fullName(user.getFullName())
                          .authorities(user.getAuthorities().stream()
                                           .map(GrantedAuthority::getAuthority)
                                           .toList())
                          .enabled(user.isEnabled())
                          .build();
  }
}