/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.email_verification.config;

import com.jonathanmarquezperez.security.security.enums.OtpPurpose;
import lombok.RequiredArgsConstructor;

/**
 * Implementación por defecto de {@link EmailTemplateProvider}.
 *
 * <p>Genera HTML usando los valores de {@link EmailBrandingProperties}.
 * El consumidor puede reemplazarla declarando su propio bean {@link EmailTemplateProvider}.</p>
 */
@RequiredArgsConstructor
public class DefaultEmailTemplateProvider implements EmailTemplateProvider {

    private final EmailBrandingProperties branding;

    @Override
    public String buildOtpTemplate(String otpCode, int expirationMinutes, OtpPurpose purpose) {

        String title = switch (purpose) {
            case EMAIL_VERIFICATION -> "Verificación de Email";
            case PASSWORD_RESET     -> "Restablecer contraseña";
            case PASSWORD_CHANGE    -> "Cambio de contraseña";
        };

        String description = switch (purpose) {
            case EMAIL_VERIFICATION -> "Para completar tu registro, utiliza el siguiente código de verificación:";
            case PASSWORD_RESET     -> "Para restablecer tu contraseña, utiliza el siguiente código:";
            case PASSWORD_CHANGE    -> "Para confirmar el cambio de contraseña, utiliza el siguiente código:";
        };

        String logoHtml = branding.getLogoUrl() == null || branding.getLogoUrl().isBlank()
                ? ""
                : String.format("<img src=\"%s\" alt=\"%s\" style=\"max-height:60px; margin-bottom:12px;\"><br>",
                        branding.getLogoUrl(), branding.getAppName());

        return String.format("""
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s</title>
                </head>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background: linear-gradient(135deg, %s 0%%, %s 100%%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                        %s
                        <h1 style="color: white; margin: 0; font-size: 28px;">%s</h1>
                    </div>
                    <div style="background-color: #f9f9f9; padding: 30px; border-radius: 0 0 10px 10px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);">
                        <p style="font-size: 16px; margin-bottom: 20px;">%s</p>

                        <div style="background-color: #ffffff; border: 2px dashed %s; border-radius: 8px; padding: 20px; text-align: center; margin: 25px 0;">
                            <p style="font-size: 14px; color: #666; margin: 0 0 10px 0;">Tu código es:</p>
                            <p style="font-size: 36px; font-weight: bold; color: %s; letter-spacing: 8px; margin: 0;">%s</p>
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

                        <p style="font-size: 12px; color: #999; text-align: center; margin: 0;">%s</p>
                    </div>
                </body>
                </html>
                """,
                title,
                branding.getPrimaryColor(), branding.getSecondaryColor(),
                logoHtml,
                title,
                description,
                branding.getPrimaryColor(),
                branding.getPrimaryColor(),
                otpCode,
                expirationMinutes,
                branding.getFooterText()
        );
    }

    @Override
    public String buildWelcomeTemplate(String firstName) {

        String logoHtml = branding.getLogoUrl() == null || branding.getLogoUrl().isBlank()
                ? ""
                : String.format("<img src=\"%s\" alt=\"%s\" style=\"max-height:60px; margin-bottom:12px;\"><br>",
                        branding.getLogoUrl(), branding.getAppName());

        return String.format("""
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Bienvenido</title>
                </head>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background: linear-gradient(135deg, %s 0%%, %s 100%%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                        %s
                        <h1 style="color: white; margin: 0; font-size: 28px;">¡Bienvenido a %s!</h1>
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

                        <p style="font-size: 12px; color: #999; text-align: center; margin: 0;">%s</p>
                    </div>
                </body>
                </html>
                """,
                branding.getPrimaryColor(), branding.getSecondaryColor(),
                logoHtml,
                branding.getAppName(),
                firstName,
                branding.getFooterText()
        );
    }
}