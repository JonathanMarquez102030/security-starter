/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.autoconfigure;

import com.jonathanmarquezperez.security.email_verification.config.DefaultEmailTemplateProvider;
import com.jonathanmarquezperez.security.email_verification.config.EmailBrandingProperties;
import com.jonathanmarquezperez.security.email_verification.config.EmailTemplateProvider;
import com.jonathanmarquezperez.security.email_verification.config.OtpProperties;
import com.jonathanmarquezperez.security.email_verification.repository.OtpTokenRepository;
import com.jonathanmarquezperez.security.email_verification.service.EmailService;
import com.jonathanmarquezperez.security.email_verification.service.OtpService;
import com.jonathanmarquezperez.security.email_verification.service.impl.EmailServiceImpl;
import com.jonathanmarquezperez.security.email_verification.service.impl.OtpServiceImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.mail.autoconfigure.MailSenderAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;

@AutoConfiguration(after = {
        SecurityCoreAutoConfiguration.class,
        MailSenderAutoConfiguration.class
})
@ConditionalOnWebApplication
@EnableConfigurationProperties(EmailBrandingProperties.class)
@ConditionalOnClass(JavaMailSender.class)
@EnableScheduling
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class OtpAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(JavaMailSender.class)
    public EmailService emailService(JavaMailSender mailSender, EmailTemplateProvider emailTemplateProvider) {
        return new EmailServiceImpl(mailSender, emailTemplateProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(EmailService.class)
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

    @Bean
    @ConditionalOnMissingBean
    public EmailTemplateProvider emailTemplateProvider(EmailBrandingProperties emailBrandingProperties) {
        return new DefaultEmailTemplateProvider(emailBrandingProperties);
    }
}
