/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.email_verification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades de marca para personalizar los correos electrónicos enviados por la librería.
 *
 * <p>Ejemplo en application.yml:</p>
 * <pre>
 * email:
 *   branding:
 *     app-name: "Mi Aplicación"
 *     primary-color: "#1a73e8"
 *     secondary-color: "#0d47a1"
 *     logo-url: "https://mi-app.com/logo.png"
 *     footer-text: "© 2026 Mi Empresa. Todos los derechos reservados."
 * </pre>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "email.branding")
public class EmailBrandingProperties {

    /**
     * Nombre de la aplicación que aparece en el encabezado del correo.
     */
    private String appName = "Security App";

    /**
     * Color primario en formato hexadecimal. Se usa en el encabezado y elementos destacados.
     */
    private String primaryColor = "#667eea";

    /**
     * Color secundario en formato hexadecimal. Se usa en el gradiente del encabezado.
     */
    private String secondaryColor = "#764ba2";

    /**
     * URL de la imagen del logo que aparece en el encabezado. Si está vacía, no se muestra logo.
     */
    private String logoUrl = "";

    /**
     * Texto del pie de página del correo.
     */
    private String footerText = "Este es un mensaje automático, por favor no respondas a este correo.";
}