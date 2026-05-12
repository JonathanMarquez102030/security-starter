/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.email_verification.service.impl;

import com.jonathanmarquezperez.email_verification.dto.OtpResponseDto;
import com.jonathanmarquezperez.email_verification.service.OtpService;
import com.jonathanmarquezperez.security.enums.OtpPurpose;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación no-operativa de OtpService.
 * Se registra automáticamente cuando spring-boot-starter-mail
 * no está en el classpath del consumidor.
 */
@Slf4j
public class NoOpOtpService implements OtpService {

    private static final String MSG =
        "OtpService no disponible: agrega spring-boot-starter-mail para habilitar esta función.";

    @Override
    public void deleteAllOtpsByEmail(String email) {
        log.debug("NoOpOtpService: deleteAllOtpsByEmail ignorado (mail no configurado)");
    }

    @Override
    public void cleanupExpiredOtps() {
        log.debug("NoOpOtpService: cleanupExpiredOtps ignorado (mail no configurado)");
    }

    @Override
    public OtpResponseDto generateAndSendOtp(String email, OtpPurpose purpose) {
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public void verifyOtp(String email, String code, OtpPurpose purpose) {
        throw new UnsupportedOperationException(MSG);
    }

    @Override
    public OtpResponseDto resendOtp(String email, OtpPurpose purpose) {
        throw new UnsupportedOperationException(MSG);
    }
}
