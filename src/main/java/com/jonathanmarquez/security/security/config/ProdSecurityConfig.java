package com.jonathanmarquez.security.security.config;

import com.jonathanmarquez.security.security.filter.CsrfCookieFilter;
import com.jonathanmarquez.security.security.filter.JWTTokenGeneratorFilter;
import com.jonathanmarquez.security.security.filter.JWTTokenValidatorFilter;
import com.jonathanmarquez.security.security.utils.CookieUtil;
import com.jonathanmarquez.security.security.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;
import java.util.List;

/**
 * Configuración de seguridad para PRODUCCIÓN.
 * Solo HTTPS, cookies secure, CORS restrictivo.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@Profile("prod")
@RequiredArgsConstructor
public class ProdSecurityConfig {

    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
  private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        configureCsrf(http);

        http.addFilterBefore(new JWTTokenValidatorFilter(jwtUtil, cookieUtil, userDetailsService), BasicAuthenticationFilter.class);
        http.addFilterAfter(new JWTTokenGeneratorFilter(jwtUtil, cookieUtil), BasicAuthenticationFilter.class);
        http.addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class);

        http.authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/api/auth/register",
                "/api/auth/csrf",
                "/api/auth/refresh",
                "/api/auth/public/**",
                "/error"
            ).permitAll()
            .requestMatchers("/api/auth/login", "/api/auth/me", "/api/auth/logout").authenticated()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
            .anyRequest().authenticated()
        );

        http.httpBasic(basic -> {});
        http.formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }

    private void configureCsrf(HttpSecurity http) throws Exception {
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName("_csrf");

        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        tokenRepository.setCookieCustomizer(cookie ->
            cookie.secure(true)  // Solo HTTPS en producción
                  .sameSite("Strict")
        );

        http.csrf(csrf -> csrf
            .csrfTokenRequestHandler(csrfHandler)
            .ignoringRequestMatchers(
                "/api/auth/login",
                "/api/auth/register",
                "/api/auth/refresh",
                "/api/auth/csrf",
                "/api/auth/logout"
            )
            .csrfTokenRepository(tokenRepository)
        );
    }

    private CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(Collections.singletonList("https://yourapp.com"));
            config.setAllowedMethods(Collections.singletonList("*"));
            config.setAllowCredentials(true);
            config.setAllowedHeaders(Collections.singletonList("*"));
            config.setExposedHeaders(List.of("Authorization"));
            config.setMaxAge(3600L);
            return config;
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}