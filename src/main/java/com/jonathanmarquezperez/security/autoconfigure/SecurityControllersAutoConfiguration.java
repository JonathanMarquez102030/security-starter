/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.autoconfigure;

import com.jonathanmarquezperez.security.email_verification.controller.VerificationController;
import com.jonathanmarquezperez.security.email_verification.service.EmailService;
import com.jonathanmarquezperez.security.email_verification.service.OtpService;
import com.jonathanmarquezperez.security.email_verification.service.impl.NoOpOtpService;
import com.jonathanmarquezperez.security.exceptions.response.ApiResponseFactory;
import com.jonathanmarquezperez.security.security.config.PasswordPolicyProperties;
import com.jonathanmarquezperez.security.security.config.PasswordPolicyValidator;
import com.jonathanmarquezperez.security.security.config.SecurityProperties;
import com.jonathanmarquezperez.security.security.controller.AuthController;
import com.jonathanmarquezperez.security.security.controller.MePasswordController;
import com.jonathanmarquezperez.security.security.controller.PasswordController;
import com.jonathanmarquezperez.security.security.model.mapper.UserProfileMapper;
import com.jonathanmarquezperez.security.security.repository.UserProfileRepository;
import com.jonathanmarquezperez.security.security.service.PasswordService;
import com.jonathanmarquezperez.security.security.service.UserProfileService;
import com.jonathanmarquezperez.security.security.service.impl.PasswordServiceImpl;
import com.jonathanmarquezperez.security.security.service.impl.UserProfileServiceImpl;
import com.jonathanmarquezperez.security.security.utils.CookieUtil;
import com.jonathanmarquezperez.security.security.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@AutoConfiguration(after = {SecurityCoreAutoConfiguration.class, OtpAutoConfiguration.class})
@ConditionalOnWebApplication
public class SecurityControllersAutoConfiguration {

    // ── Mapper ────────────────────────────────────────────────────────────────────

    @Bean
    @ConditionalOnMissingBean
    public UserProfileMapper userProfileMapper() {
        return Mappers.getMapper(UserProfileMapper.class);
    }

    // ── ApiResponseFactory ────────────────────────────────────────────────────────

    @Bean
    @ConditionalOnMissingBean
    public ApiResponseFactory apiResponseFactory(HttpServletRequest request) {
        return new ApiResponseFactory(request);
    }

    // ── Servicios ─────────────────────────────────────────────────────────────────

    @Bean
    @ConditionalOnMissingBean(OtpService.class)   // solo si OtpAutoConfiguration NO cargó
    public OtpService noOpOtpService() {
        return new NoOpOtpService();
    }


    @Bean
    @ConditionalOnMissingBean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public UserProfileService userProfileService(
            JdbcTemplate jdbcTemplate,
            UserProfileRepository userProfileRepository,
            PasswordEncoder passwordEncoder,
            UserDetailsService userDetailsService,
            SecurityProperties securityProperties,
            UserProfileMapper userProfileMapper,
            OtpService otpService,
            PasswordPolicyValidator passwordPolicyValidator) {
        return new UserProfileServiceImpl(
                jdbcTemplate, userProfileRepository, passwordEncoder,
                userDetailsService, securityProperties, userProfileMapper, otpService,
                passwordPolicyValidator);
    }

    @Bean
    @ConditionalOnMissingBean
    public PasswordService passwordService(
            UserProfileService userProfileService,
            OtpService otpService,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder,
            PasswordPolicyValidator passwordPolicyValidator) {
        return new PasswordServiceImpl(
                userProfileService, otpService, jwtUtil, passwordEncoder, passwordPolicyValidator);
    }

    @Bean
    @ConditionalOnMissingBean
    public PasswordPolicyValidator passwordPolicyValidator(PasswordPolicyProperties passwordPolicyProperties) {
        return new PasswordPolicyValidator(passwordPolicyProperties);
    }

    // ── Controladores de seguridad ────────────────────────────────────────────────

    @Bean
    @ConditionalOnMissingBean
    public AuthController authController(
            UserProfileService userProfileService,
            JwtUtil jwtUtil,
            CookieUtil cookieUtil,
            UserProfileMapper userProfileMapper,
            OtpService otpService,
            ApiResponseFactory apiResponseFactory) {
        return new AuthController(
                userProfileService, jwtUtil, cookieUtil,
                userProfileMapper, otpService, apiResponseFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public PasswordController passwordController(
            PasswordService passwordService,
            ApiResponseFactory apiResponseFactory,
            CookieUtil cookieUtil) {
        return new PasswordController(passwordService, apiResponseFactory, cookieUtil);
    }

    @Bean
    @ConditionalOnMissingBean
    public MePasswordController mePasswordController(
            PasswordService passwordService,
            ApiResponseFactory apiResponseFactory,
            CookieUtil cookieUtil) {
        return new MePasswordController(passwordService, apiResponseFactory, cookieUtil);
    }

    // ── Controlador OTP (solo si el módulo OTP está activo) ───────────────────────

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(EmailService.class)
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public VerificationController verificationController(
            OtpService otpService,
            UserProfileService userProfileService,
            EmailService emailService,
            UserProfileRepository userProfileRepository,
            ApiResponseFactory apiResponseFactory) {
        return new VerificationController(
                otpService, userProfileService, emailService,
                userProfileRepository, apiResponseFactory);
    }
}
