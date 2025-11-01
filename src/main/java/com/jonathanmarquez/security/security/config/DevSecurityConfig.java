package com.jonathanmarquez.security.security.config;

import com.jonathanmarquez.security.security.filter.CsrfCookieFilter;
import com.jonathanmarquez.security.security.filter.JWTTokenGeneratorFilter;
import com.jonathanmarquez.security.security.filter.JWTTokenValidatorFilter;
import com.jonathanmarquez.security.security.utils.CookieUtil;
import com.jonathanmarquez.security.security.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
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
 * Configuración de seguridad para DESARROLLO.
 * Permite HTTP, cookies sin secure, y CORS permisivo.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@Profile("dev")
@RequiredArgsConstructor
public class DevSecurityConfig {

  private final JwtUtil jwtUtil;
  private final CookieUtil cookieUtil;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    // 1. Sesión stateless
    http.sessionManagement(session ->
                               session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    // 2. CORS (permisivo para desarrollo)
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

    // 3. CSRF con cookies (seguro pero no httpOnly para JavaScript)
    configureCsrf(http);

    // 4. Filtros JWT personalizados
    http.addFilterBefore(new JWTTokenValidatorFilter(jwtUtil, cookieUtil), BasicAuthenticationFilter.class);
    http.addFilterAfter(new JWTTokenGeneratorFilter(jwtUtil, cookieUtil), BasicAuthenticationFilter.class);
    http.addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class);

    // 5. Reglas de autorización
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers(
            "/api/auth/register",
            "/api/auth/csrf",
            "/api/auth/refresh",
            "/api/auth/public/**",
            "/error"
        ).permitAll()
        .requestMatchers("/api/auth/login", "/api/auth/me",  "/api/auth/logout").authenticated()
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
        .anyRequest().authenticated()
    );

    // 6. HTTP Basic (solo para login inicial)
    http.httpBasic(basic -> {
    });

    // 7. Deshabilitar form login
    http.formLogin(AbstractHttpConfigurer::disable);

    return http.build();
  }

  private void configureCsrf(HttpSecurity http) throws Exception {
    CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
    csrfHandler.setCsrfRequestAttributeName("_csrf");

    CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
    tokenRepository.setCookieCustomizer(cookie ->
                                            cookie.secure(false)  // Permite HTTP en desarrollo
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
    return new CorsConfigurationSource() {
      @Override
      public CorsConfiguration getCorsConfiguration(@NonNull HttpServletRequest request) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:3000"));
        config.setAllowedMethods(Collections.singletonList("*"));
        config.setAllowCredentials(true);
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setExposedHeaders(List.of("Authorization", "X-XSRF-TOKEN"));
        config.setMaxAge(3600L);
        return config;
      }
    };
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
    provider.setUserDetailsService(userDetailsService);
    provider.setPasswordEncoder(passwordEncoder);

    ProviderManager providerManager = new ProviderManager(provider);
    providerManager.setEraseCredentialsAfterAuthentication(false);
    return providerManager;
  }
}