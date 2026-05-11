/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.email_verification.service.impl;

import com.jonathanmarquezperez.security.email_verification.config.EmailTemplateProvider;
import com.jonathanmarquezperez.security.email_verification.service.EmailService;
import com.jonathanmarquezperez.security.security.enums.OtpPurpose;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * Implementación del servicio de email usando Spring Mail.
 */
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

  private final JavaMailSender mailSender;
  private final EmailTemplateProvider emailTemplateProvider;

  @Value("${spring.mail.username}")
  private String fromEmail;


  @Override
  public void sendOtpEmail(String toEmail, String otpCode, Integer expirationMinutes, OtpPurpose purpose) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(toEmail);
      helper.setSubject(emailTemplateProvider.buildOtpSubject(purpose));
      helper.setText(emailTemplateProvider.buildOtpTemplate(otpCode, expirationMinutes == null ? 0 : expirationMinutes, purpose), true);

      mailSender.send(message);

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
      helper.setSubject(emailTemplateProvider.buildWelcomeSubject());
      helper.setText(emailTemplateProvider.buildWelcomeTemplate(firstName), true);

      mailSender.send(message);
      log.info("Email de bienvenida enviado exitosamente a: {}", to);

    } catch (MessagingException e) {
      log.error("Error al enviar email de bienvenida a {}: {}", to, e.getMessage(), e);
      // No lanzamos excepción aquí porque la verificación ya fue exitosa
    }
  }
}
