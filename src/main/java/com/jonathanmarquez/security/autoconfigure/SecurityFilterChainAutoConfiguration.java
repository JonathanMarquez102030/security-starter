/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquez.security.autoconfigure;

import com.jonathanmarquez.security.security.config.CustomAccessDeniedHandler;
import com.jonathanmarquez.security.security.config.CustomBasicAuthenticationEntryPoint;
import com.jonathanmarquez.security.security.config.SecurityProperties;
import com.jonathanmarquez.security.security.filter.CsrfCookieFilter;
import com.jonathanmarquez.security.security.filter.JWTTokenGeneratorFilter;
import com.jonathanmarquez.security.security.filter.JWTTokenValidatorFilter;
import com.jonathanmarquez.security.security.utils.CookieUtil;
import com.jonathanmarquez.security.security.utils.JwtUtil;
import com.jonathanmarquez.security.utils.ProfileDetector;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.ObjectMapper;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@AutoConfiguration(after = SecurityCoreAutoConfiguration.class)
@ConditionalOnWebApplication
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityFilterChainAutoConfiguration {

    // ── Handlers ─────────────────────────────────────────────────────────────────

    @Bean
    @ConditionalOnMissingBean
    public CustomBasicAuthenticationEntryPoint authenticationEntryPoint(ProfileDetector profileDetector,
                                                                        ObjectMapper objectMapper) {
        return new CustomBasicAuthenticationEntryPoint(profileDetector, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public CustomAccessDeniedHandler accessDeniedHandler(ProfileDetector profileDetector,
                                                         ObjectMapper objectMapper) {
        return new CustomAccessDeniedHandler(profileDetector, objectMapper);
    }

    // ── SecurityFilterChain ───────────────────────────────────────────────────────

    @Bean
    @ConditionalOnMissingBean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtUtil jwtUtil,
            CookieUtil cookieUtil,
            UserDetailsService userDetailsService,
            CustomBasicAuthenticationEntryPoint authenticationEntryPoint,
            CustomAccessDeniedHandler accessDeniedHandler,
            SecurityProperties props) {

        // 1. Sesión stateless
        http.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 2. CORS — valores vienen del consumidor vía SecurityProperties
        http.cors(cors -> cors.configurationSource(buildCorsSource(props.getCors())));

        // 3. CSRF — rutas ignoradas y secure-flag vienen de properties
        configureCsrf(http, props);

        // 4. Filtros JWT personalizados
        http.addFilterBefore(
                new JWTTokenValidatorFilter(jwtUtil, cookieUtil, userDetailsService),
                BasicAuthenticationFilter.class);
        http.addFilterAfter(
                new JWTTokenGeneratorFilter(jwtUtil, cookieUtil),
                BasicAuthenticationFilter.class);
        http.addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class);

        // 5. Reglas de autorización — publicPaths configurable, resto son defaults razonables
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers(props.getAuthorization().getPublicPaths().toArray(String[]::new))
                .permitAll()
            .requestMatchers("/auth/login", "/auth/me", "/me/**", "/auth/logout")
                .authenticated()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
            .anyRequest().authenticated()
        );

        // 6. Deshabilitar form login
        http.formLogin(AbstractHttpConfigurer::disable);

        // 7. HTTP Basic + manejadores personalizados
        http.httpBasic(hbc -> hbc.authenticationEntryPoint(authenticationEntryPoint));
        http.exceptionHandling(ehc -> ehc.accessDeniedHandler(accessDeniedHandler));

        return http.build();
    }

    // ── Helpers privados ─────────────────────────────────────────────────────────

    private CorsConfigurationSource buildCorsSource(SecurityProperties.Cors corsProps) {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(corsProps.getAllowedOrigins());
            config.setAllowedMethods(corsProps.getAllowedMethods());
            config.setAllowCredentials(corsProps.isAllowCredentials());
            config.setAllowedHeaders(corsProps.getAllowedHeaders());
            config.setExposedHeaders(corsProps.getExposedHeaders());
            config.setMaxAge(corsProps.getMaxAge());
            return config;
        };
    }

    private void configureCsrf(HttpSecurity http, SecurityProperties props) {
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName("_csrf");

        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        tokenRepository.setCookieCustomizer(cookie -> cookie
            .secure(props.getCookie().isSecure())
            .sameSite(props.getCookie().getSameSite())
            .path("/")
        );

        http.csrf(csrf -> csrf
            .csrfTokenRequestHandler(csrfHandler)
            .ignoringRequestMatchers(
                props.getCsrf().getIgnoredPaths().toArray(String[]::new))
            .csrfTokenRepository(tokenRepository)
        );
    }
}
