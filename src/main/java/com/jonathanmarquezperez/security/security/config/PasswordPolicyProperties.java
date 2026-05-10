package com.jonathanmarquezperez.security.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propiedades configurables para la política de contraseñas.
 * Se aplica en registro, cambio y recuperación de contraseña.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "password.policy")
public class PasswordPolicyProperties {

    /**
     * Longitud mínima permitida para la contraseña.
     */
    private int minLength = 8;

    /**
     * Expresión regular que debe cumplir la contraseña.
     * Por defecto requiere al menos una minúscula, una mayúscula y un dígito.
     */
    private String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$";

    /**
     * Mensaje que se muestra cuando la contraseña no cumple la política de complejidad.
     */
    private String complexityMessage = "La contraseña no cumple la política de complejidad requerida";
}