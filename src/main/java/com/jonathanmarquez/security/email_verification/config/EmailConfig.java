package com.jonathanmarquez.security.email_verification.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuración para el servicio de correo electrónico.
 */
@Configuration
public class EmailConfig {

  /**
   * El JavaMailSender ya está configurado automáticamente por Spring Boot
   * a través de las propiedades en application.yaml (spring.mail.*).
   * 
   * Este método está aquí solo como referencia en caso de necesitar
   * configuración adicional personalizada.
   */
  // El bean JavaMailSender se crea automáticamente por Spring Boot
  // No es necesario declararlo manualmente si usamos spring.mail.* properties
}