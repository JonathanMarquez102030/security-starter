package com.jonathanmarquez.security.autoconfigure;

import com.jonathanmarquez.security.email_verification.controller.VerificationController;
import com.jonathanmarquez.security.email_verification.service.EmailService;
import com.jonathanmarquez.security.email_verification.service.OtpService;
import com.jonathanmarquez.security.exceptions.response.ApiResponseFactory;
import com.jonathanmarquez.security.security.config.SecurityProperties;
import com.jonathanmarquez.security.security.controller.AuthController;
import com.jonathanmarquez.security.security.controller.MePasswordController;
import com.jonathanmarquez.security.security.controller.PasswordController;
import com.jonathanmarquez.security.security.model.mapper.UserProfileMapper;
import com.jonathanmarquez.security.security.repository.UserProfileRepository;
import com.jonathanmarquez.security.security.service.PasswordService;
import com.jonathanmarquez.security.security.service.UserProfileService;
import com.jonathanmarquez.security.security.service.impl.PasswordServiceImpl;
import com.jonathanmarquez.security.security.service.impl.UserProfileServiceImpl;
import com.jonathanmarquez.security.security.utils.CookieUtil;
import com.jonathanmarquez.security.security.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
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
    @ConditionalOnMissingBean
    public UserProfileService userProfileService(
            JdbcTemplate jdbcTemplate,
            UserProfileRepository userProfileRepository,
            PasswordEncoder passwordEncoder,
            UserDetailsService userDetailsService,
            SecurityProperties securityProperties,
            UserProfileMapper userProfileMapper,
            OtpService otpService) {
        return new UserProfileServiceImpl(
                jdbcTemplate, userProfileRepository, passwordEncoder,
                userDetailsService, securityProperties, userProfileMapper, otpService);
    }

    @Bean
    @ConditionalOnMissingBean
    public PasswordService passwordService(
            UserProfileService userProfileService,
            OtpService otpService,
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder,
            Environment env) {
        return new PasswordServiceImpl(
                userProfileService, otpService, jwtUtil, passwordEncoder, env);
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
    @ConditionalOnBean(OtpService.class)   // solo si OtpAutoConfiguration cargó el OtpService
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