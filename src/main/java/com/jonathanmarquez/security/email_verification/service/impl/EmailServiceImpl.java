package com.jonathanmarquez.security.email_verification.service.impl;

import com.jonathanmarquez.security.email_verification.service.EmailService;
import com.jonathanmarquez.security.security.enums.OtpPurpose;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Implementación del servicio de email usando Spring Mail.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

  private final JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String fromEmail;

  @Override
  public void sendOtpEmail(String to, String otpCode, Integer expirationMinutes) {
    sendOtpEmail(to, otpCode, expirationMinutes, OtpPurpose.EMAIL_VERIFICATION);
  }

  @Override
  public void sendOtpEmail(String toEmail, String otpCode, Integer expirationMinutes, OtpPurpose purpose) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(toEmail);
      helper.setSubject(buildOtpSubject(purpose));
      helper.setText(buildOtpEmailTemplate(otpCode, expirationMinutes, purpose), true); // HTML=true

      mailSender.send(message);

      // No loggear el OTP ni el body
      log.info("Email OTP enviado exitosamente a: {} (purpose={})", toEmail, purpose);

    } catch (MessagingException e) {
      log.error("Error al enviar email OTP a {} (purpose={}): {}", toEmail, purpose, e.getMessage(), e);
      throw new RuntimeException("Error al enviar email OTP", e);
    }
  }

  @Override
  public void sendWelcomeEmail(String to, String firstName) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setSubject("¡Bienvenido! Tu cuenta ha sido verificada");
      helper.setText(buildWelcomeEmailTemplate(firstName), true);

      mailSender.send(message);
      log.info("Email de bienvenida enviado exitosamente a: {}", to);

    } catch (MessagingException e) {
      log.error("Error al enviar email de bienvenida a {}: {}", to, e.getMessage(), e);
      // No lanzamos excepción aquí porque la verificación ya fue exitosa
    }
  }

  private String buildOtpSubject(OtpPurpose purpose) {
    return switch (purpose) {
      case EMAIL_VERIFICATION -> "Verificación de Email - Código OTP";
      case PASSWORD_RESET -> "Restablecer contraseña - Código OTP";
      case PASSWORD_CHANGE -> "Cambio de contraseña - Código OTP";
    };
  }

  /**
   * Construye el template HTML para el email de OTP dependiendo del propósito.
   */
  private String buildOtpEmailTemplate(String otpCode, Integer expirationMinutes, OtpPurpose purpose) {
    int minutes = expirationMinutes == null ? 0 : expirationMinutes;

    String title = switch (purpose) {
      case EMAIL_VERIFICATION -> "Verificación de Email";
      case PASSWORD_RESET -> "Restablecer contraseña";
      case PASSWORD_CHANGE -> "Cambio de contraseña";
    };

    String description = switch (purpose) {
      case EMAIL_VERIFICATION -> "Para completar tu registro, utiliza el siguiente código de verificación:";
      case PASSWORD_RESET -> "Para restablecer tu contraseña, utiliza el siguiente código:";
      case PASSWORD_CHANGE -> "Para confirmar el cambio de contraseña, utiliza el siguiente código:";
    };

    return String.format("""
                             <!DOCTYPE html>
                             <html lang="es">
                             <head>
                                 <meta charset="UTF-8">
                                 <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                 <title>%s</title>
                             </head>
                             <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                                 <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                                     <h1 style="color: white; margin: 0; font-size: 28px;">%s</h1>
                                 </div>
                                 <div style="background-color: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);">
                                     <p style="font-size: 16px; margin-bottom: 20px;">%s</p>
                             
                                     <div style="background-color: #ffffff; border: 2px dashed #667eea; border-radius: 8px; padding: 20px; text-align: center; margin: 25px 0;">
                                         <p style="font-size: 14px; color: #666; margin: 0 0 10px 0;">Tu código es:</p>
                                         <p style="font-size: 36px; font-weight: bold; color: #667eea; letter-spacing: 8px; margin: 0;">%s</p>
                                     </div>
                             
                                     <div style="background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 12px; margin: 20px 0; border-radius: 4px;">
                                         <p style="margin: 0; font-size: 14px; color: #856404;">
                                             Este código expirará en <strong>%d minutos</strong>
                                         </p>
                                     </div>
                             
                                     <p style="font-size: 14px; color: #666; margin-top: 25px;">
                                         Si tú no solicitaste esto, ignora este correo.
                                     </p>
                             
                                     <hr style="border: none; border-top: 1px solid #ddd; margin: 25px 0;">
                             
                                     <p style="font-size: 12px; color: #999; text-align: center; margin: 0;">
                                         Este es un mensaje automático, por favor no respondas a este correo.
                                     </p>
                                 </div>
                             </body>
                             </html>
                             """, title, title, description, otpCode, minutes);
  }

  /**
   * Construye el template HTML para el email de bienvenida.
   */
  private String buildWelcomeEmailTemplate(String firstName) {
    return String.format("""
                             <!DOCTYPE html>
                             <html lang="es">
                             <head>
                                 <meta charset="UTF-8">
                                 <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                 <title>Bienvenido</title>
                             </head>
                             <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                                 <div style="background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                                     <h1 style="color: white; margin: 0; font-size: 28px;">¡Bienvenido!</h1>
                                 </div>
                                 <div style="background-color: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);">
                                     <p style="font-size: 18px; margin-bottom: 20px;">
                                         Hola <strong>%s</strong>,
                                     </p>
                             
                                     <p style="font-size: 16px; margin-bottom: 20px;">
                                         Tu cuenta ha sido verificada exitosamente. Ahora puedes acceder a todas las funcionalidades de nuestra plataforma.
                                     </p>
                             
                                     <div style="text-align: center; margin: 30px 0;">
                                         <div style="background-color: #d4edda; border: 2px solid #28a745; border-radius: 8px; padding: 20px; display: inline-block;">
                                             <p style="font-size: 48px; margin: 0;">✓</p>
                                             <p style="font-size: 18px; color: #155724; margin: 10px 0 0 0; font-weight: bold;">
                                                 ¡Cuenta Verificada!
                                             </p>
                                         </div>
                                     </div>
                             
                                     <p style="font-size: 16px; margin-bottom: 20px;">
                                         Estamos emocionados de tenerte con nosotros. Si tienes alguna pregunta, no dudes en contactarnos.
                                     </p>
                             
                                     <hr style="border: none; border-top: 1px solid #ddd; margin: 25px 0;">
                             
                                     <p style="font-size: 12px; color: #999; text-align: center; margin: 0;">
                                         Este es un mensaje automático, por favor no respondas a este correo.
                                     </p>
                                 </div>
                             </body>
                             </html>
                             """, firstName);
  }
}