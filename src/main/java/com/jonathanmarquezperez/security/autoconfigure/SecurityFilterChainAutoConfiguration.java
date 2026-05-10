/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.autoconfigure;

import com.jonathanmarquezperez.security.security.config.CustomAccessDeniedHandler;
import com.jonathanmarquezperez.security.security.config.CustomBasicAuthenticationEntryPoint;
import com.jonathanmarquezperez.security.security.config.SecurityProperties;
import com.jonathanmarquezperez.security.security.filter.CsrfCookieFilter;
import com.jonathanmarquezperez.security.security.filter.JWTTokenGeneratorFilter;
import com.jonathanmarquezperez.security.security.filter.JWTTokenValidatorFilter;
import com.jonathanmarquezperez.security.security.utils.CookieUtil;
import com.jonathanmarquezperez.security.security.utils.JwtUtil;
import com.jonathanmarquezperez.security.utils.ProfileDetector;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Stream;

@AutoConfiguration(after = SecurityCoreAutoConfiguration.class)
@ConditionalOnWebApplication
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
public class SecurityFilterChainAutoConfiguration {

    // ── Constantes ─────────────────────────────────────────────────────────────────

    // Paths que la librería SIEMPRE protege — el consumidor no puede eliminarlos
    private static final List<String> LIBRARY_PUBLIC_PATHS = List.of(
            "/auth/register", "/auth/csrf", "/auth/refresh",
            "/auth/public/**", "/auth/verify", "/auth/resend-otp",
            "/auth/password/**", "/error"
    );

    private static final List<String> LIBRARY_AUTHENTICATED_PATHS = List.of(
            "/auth/login", "/auth/me", "/me/**", "/auth/logout"
    );

    private static final List<String> LIBRARY_CSRF_IGNORED_PATHS = List.of(
            "/auth/register", "/auth/csrf", "/auth/refresh",
            "/auth/public/**", "/auth/login", "/auth/logout",
            "/auth/verify", "/auth/resend-otp", "/auth/password/**"
    );

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

        // 5. Reglas de autorización
        String[] publicPaths = mergePaths(LIBRARY_PUBLIC_PATHS, props.getAuthorization().getPublicPaths());
        String[] authenticatedPaths = mergePaths(LIBRARY_AUTHENTICATED_PATHS, props.getAuthorization().getAuthenticatedPaths());

        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers(publicPaths).permitAll();
            applyConsumerRules(auth, props.getAuthorization().getRules());
            auth.requestMatchers(authenticatedPaths).authenticated();
            auth.anyRequest().authenticated();
        });

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

        String[] csrfIgnoredPaths = mergePaths(
                LIBRARY_CSRF_IGNORED_PATHS,
                props.getCsrf().getIgnoredPaths()
        );

        http.csrf(csrf -> csrf
            .csrfTokenRequestHandler(csrfHandler)
                .ignoringRequestMatchers(csrfIgnoredPaths)
            .csrfTokenRepository(tokenRepository)
        );
    }

    /**
     * Fusiona los paths internos de la librería con los paths adicionales del consumidor.
     * Elimina duplicados. El orden es: librería primero, consumidor después.
     */
    private String[] mergePaths(List<String> libraryPaths, List<String> consumerPaths) {
        return Stream.concat(libraryPaths.stream(), consumerPaths.stream())
                .distinct()
                .toArray(String[]::new);
    }

    /**
     * Aplica las reglas de autorización definidas por el consumidor en el orden declarado.
     * Usa la API nativa de Spring Security 7.x para todos los tipos de regla.
     */
    private void applyConsumerRules(
            AuthorizeHttpRequestsConfigurer<?>.AuthorizationManagerRequestMatcherRegistry auth,
            List<SecurityProperties.AuthorizationRule> rules) {

        for (SecurityProperties.AuthorizationRule rule : rules) {

            if (rule.getPaths().isEmpty()) continue;

            String[] paths = rule.getPaths().toArray(String[]::new);

            switch (rule.getType()) {

                case PERMIT_ALL -> auth.requestMatchers(paths).permitAll();

                case AUTHENTICATED -> auth.requestMatchers(paths).authenticated();

                case DENY_ALL -> auth.requestMatchers(paths).denyAll();

                case HAS_ROLE -> {
                    if (rule.getRoles().isEmpty()) verifyRule(rule, true);
                    auth.requestMatchers(paths).hasRole(rule.getRoles().getFirst());
                }


                case HAS_ANY_ROLE -> {
                    if (rule.getRoles().isEmpty()) verifyRule(rule, true);
                    auth.requestMatchers(paths)
                            .hasAnyRole(rule.getRoles().toArray(String[]::new));
                }

                case HAS_ALL_ROLES -> {
                    if (rule.getRoles().isEmpty()) verifyRule(rule, true);

                    auth.requestMatchers(paths)
                            .hasAllRoles(rule.getRoles().toArray(String[]::new));
                }

                case HAS_AUTHORITY -> {
                    if (rule.getAuthorities().isEmpty()) verifyRule(rule, false);
                    auth.requestMatchers(paths).hasAuthority(rule.getAuthorities().getFirst());
                }

                case HAS_ANY_AUTHORITY -> {
                    if (rule.getAuthorities().isEmpty()) verifyRule(rule, false);
                    auth.requestMatchers(paths)
                            .hasAnyAuthority(rule.getAuthorities().toArray(String[]::new));
                }

                case HAS_ALL_AUTHORITIES -> {
                    if (rule.getAuthorities().isEmpty()) verifyRule(rule, false);
                    auth.requestMatchers(paths)
                            .hasAllAuthorities(rule.getAuthorities().toArray(String[]::new));
                }
            }
        }
    }

    private void verifyRule(SecurityProperties.AuthorizationRule rule, boolean isRoleRule) {
        boolean isEmpty = isRoleRule ? rule.getRoles().isEmpty() : rule.getAuthorities().isEmpty();
        String nameRule = isRoleRule ? "roles" : "authorities";
        if (isEmpty)
            throw new IllegalStateException(
                    "La regla " + rule.getType() + " requiere al menos un valor en " + nameRule + " . Paths: " + rule.getPaths());
    }
}
