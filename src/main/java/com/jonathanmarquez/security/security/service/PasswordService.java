package com.jonathanmarquez.security.security.service;


import com.jonathanmarquez.security.security.model.dto.ChangePasswordConfirmRequestDto;

/**
 * Interfaz de servicio para gestionar operaciones relacionadas con contraseñas de usuario.
 *
 * <p>Define los contratos para los flujos de recuperación de contraseña olvidada y cambio
 * de contraseña autenticado. Ambos procesos utilizan verificación mediante códigos OTP
 * para garantizar la seguridad de las operaciones.</p>
 */
public interface PasswordService {

  /**
   * Solicita el envío de un código OTP para iniciar el proceso de recuperación de contraseña.
   *
   * @param email dirección de correo electrónico del usuario que solicita el restablecimiento
   */
  void requestForgotPasswordOtp(String email);

  /**
   * Verifica el código OTP proporcionado y emite un token JWT para restablecer la contraseña.
   *
   * @param email   dirección de correo electrónico del usuario
   * @param otpCode código de un solo uso enviado al correo del usuario
   * @return token JWT válido para restablecer la contraseña
   */
  String verifyForgotPasswordOtpAndIssueResetToken(String email, String otpCode);

  /**
   * Restablece la contraseña del usuario utilizando un token de restablecimiento válido.
   *
   * @param resetToken      token JWT de restablecimiento de contraseña previamente emitido
   * @param newPassword     nueva contraseña propuesta por el usuario
   * @param confirmPassword confirmación de la nueva contraseña
   */
  void resetPassword(String resetToken, String newPassword, String confirmPassword);

  /**
   * Solicita el envío de un código OTP para cambiar la contraseña de un usuario autenticado.
   *
   * @param authenticatedEmail dirección de correo electrónico del usuario autenticado
   */
  void requestChangePasswordOtp(String authenticatedEmail);

  /**
   * Confirma y ejecuta el cambio de contraseña tras validar la contraseña actual y el código OTP.
   *
   * @param authenticatedEmail              dirección de correo electrónico del usuario autenticado
   * @param changePasswordConfirmRequestDto datos de la solicitud conteniendo contraseña actual, nueva contraseña, confirmación y código OTP
   */
  void confirmChangePassword(
      String authenticatedEmail,
      ChangePasswordConfirmRequestDto changePasswordConfirmRequestDto
  );
}