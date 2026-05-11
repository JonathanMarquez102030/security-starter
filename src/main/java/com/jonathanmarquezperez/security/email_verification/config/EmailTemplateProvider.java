/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.email_verification.config;

import com.jonathanmarquezperez.security.security.enums.OtpPurpose;

/**
 * Proveedor de plantillas HTML para los correos electrónicos de la librería.
 *
 * <p>La librería registra un bean por defecto ({@code DefaultEmailTemplateProvider}) que
 * usa las properties de {@link EmailBrandingProperties} para personalizar colores,
 * nombre de app y footer.</p>
 *
 * <p>Para control total del HTML, declara tu propio bean en tu proyecto:</p>
 * <pre>
 * {@code
 * @Bean
 * public EmailTemplateProvider emailTemplateProvider() {
 *     return new MiEmailTemplateProvider();
 * }
 * }
 * </pre>
 */
public interface EmailTemplateProvider {

    /**
     * Construye el HTML del correo OTP según el propósito.
     *
     * @param otpCode           código OTP generado
     * @param expirationMinutes minutos de validez del código
     * @param purpose           propósito del OTP (verificación, reseteo, cambio de contraseña)
     * @return HTML completo del correo
     */
    String buildOtpTemplate(String otpCode, int expirationMinutes, OtpPurpose purpose);

    /**
     * Construye el HTML del correo de bienvenida tras verificar el email.
     *
     * @param firstName nombre del usuario
     * @return HTML completo del correo
     */
    String buildWelcomeTemplate(String firstName);

    /**
     * Retorna el asunto del correo OTP según el propósito.
     * Puedes sobreescribir esta función para traducir o personalizar el asunto.
     */
    default String buildOtpSubject(OtpPurpose purpose) {
        return switch (purpose) {
            case EMAIL_VERIFICATION -> "Verificación de Email - Código OTP";
            case PASSWORD_RESET     -> "Restablecer contraseña - Código OTP";
            case PASSWORD_CHANGE    -> "Cambio de contraseña - Código OTP";
        };
    }

    /**
     * Retorna el asunto del correo de bienvenida.
     * Puedes sobreescribir esta función para personalizar el asunto.
     */
    default String buildWelcomeSubject() {
        return "¡Bienvenido! Tu cuenta ha sido verificada";
    }
}