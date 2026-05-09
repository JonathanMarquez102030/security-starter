/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquez.security.autoconfigure;

import com.jonathanmarquez.security.jpa.UserAccountAuditorAware;
import com.jonathanmarquez.security.security.config.CustomUserDetailsService;
import com.jonathanmarquez.security.security.config.SecurityProperties;
import com.jonathanmarquez.security.security.repository.UserProfileRepository;
import com.jonathanmarquez.security.security.utils.CookieUtil;
import com.jonathanmarquez.security.security.utils.JwtUtil;
import com.jonathanmarquez.security.utils.ProfileDetector;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@AutoConfiguration(after = {
    DataSourceAutoConfiguration.class,
    SecurityJwtAutoConfiguration.class
})
@ConditionalOnWebApplication
@Import(SecurityCoreAutoConfiguration.JpaAuditingConfiguration.class)
public class SecurityCoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ProfileDetector profileDetector(Environment env) {
        return new ProfileDetector(env);
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtUtil jwtUtil(Environment env) {
        return new JwtUtil(env);
    }

    @Bean
    @ConditionalOnMissingBean
    public CookieUtil cookieUtil(Environment env) {
        return new CookieUtil(env);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditorAware<String> auditorAware() {
        return new UserAccountAuditorAware();
    }

    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean(UserDetailsService.class)
    public UserDetailsService userDetailsService(
            JdbcTemplate jdbcTemplate,
            UserProfileRepository userProfileRepository,
            SecurityProperties securityProperties) {
        return new CustomUserDetailsService(jdbcTemplate, userProfileRepository, securityProperties);
    }

    // ─── JPA Auditing ────────────────────────────────────────────────────────────
    // Separado en clase interna para no interferir con @DataJpaTest slices.
    // Si el consumidor ya configuró su propio JpaAuditingHandler, esta clase
    // no se carga gracias a @ConditionalOnMissingBean.
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnMissingBean(name = "jpaAuditingHandler")
    @EnableJpaAuditing
    static class JpaAuditingConfiguration {
        // vacía — su presencia activa @EnableJpaAuditing condicionalmente
    }
}