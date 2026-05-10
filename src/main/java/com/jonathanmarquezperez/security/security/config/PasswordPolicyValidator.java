package com.jonathanmarquezperez.security.security.config;

import com.jonathanmarquezperez.security.exceptions.customexceptions.PasswordPolicyException;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * Validador centralizado de política de contraseñas.
 *
 * <p>Compila el regex configurado UNA sola vez al instanciarse (arranque de la app)
 * en lugar de compilarlo en cada validación, lo que mejora el rendimiento bajo carga.</p>
 *
 * <p>Se aplica en todos los flujos que aceptan una nueva contraseña:
 * registro, cambio de contraseña y recuperación de contraseña.</p>
 */
@Slf4j
public class PasswordPolicyValidator {

    private final PasswordPolicyProperties props;
    private final Pattern compiledPattern;

    public PasswordPolicyValidator(PasswordPolicyProperties props) {
        this.props = props;
        this.compiledPattern = Pattern.compile(props.getRegex());
        log.debug("PasswordPolicyValidator inicializado (minLength={}, regex={})",
            props.getMinLength(), props.getRegex());
    }

    /**
     * Valida que la contraseña cumpla la política configurada.
     *
     * @param password contraseña en texto plano a validar
     * @throws PasswordPolicyException si la contraseña no cumple la longitud mínima o el regex
     */
    public void validate(String password) {
        log.debug("Validando política de contraseña");

        if (password == null || password.length() < props.getMinLength()) {
            log.debug("Contraseña rechazada: longitud {} < mínimo requerido {}",
                password != null ? password.length() : 0, props.getMinLength());
            throw new PasswordPolicyException(
                "La contraseña debe tener al menos " + props.getMinLength() + " caracteres"
            );
        }

        if (!compiledPattern.matcher(password).matches()) {
            log.debug("Contraseña rechazada: no cumple el regex de complejidad");
            throw new PasswordPolicyException(props.getComplexityMessage());
        }

        log.debug("Contraseña válida según política configurada");
    }
}