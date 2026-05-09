/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquez.security.autoconfigure;

import com.jonathanmarquez.security.email_verification.config.OtpProperties;
import com.jonathanmarquez.security.email_verification.repository.OtpTokenRepository;
import com.jonathanmarquez.security.email_verification.service.EmailService;
import com.jonathanmarquez.security.email_verification.service.OtpService;
import com.jonathanmarquez.security.email_verification.service.impl.EmailServiceImpl;
import com.jonathanmarquez.security.email_verification.service.impl.OtpServiceImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;

@AutoConfiguration(after = SecurityCoreAutoConfiguration.class)
@ConditionalOnWebApplication
@ConditionalOnClass(JavaMailSender.class)   // solo si el consumidor incluyó spring-boot-starter-mail
@EnableScheduling                            // activa @Scheduled en OtpServiceImpl (limpieza automática de OTPs)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection") // los repos JPA se resuelven en runtime vía @AutoConfigurationPackage
public class OtpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EmailService emailService(JavaMailSender mailSender) {
        return new EmailServiceImpl(mailSender);
    }

    @Bean
    @ConditionalOnMissingBean
    public OtpService otpService(
            OtpTokenRepository otpTokenRepository,
            EmailService emailService,
            OtpProperties otpProperties,
            JdbcTemplate jdbcTemplate) {
        return new OtpServiceImpl(
                otpTokenRepository,
                emailService,
                otpProperties,
                jdbcTemplate);
    }
}