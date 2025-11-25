package com.jonathanmarquez.security.security.config;

import com.jonathanmarquez.security.security.filter.CsrfCookieFilter;
import com.jonathanmarquez.security.security.filter.JWTTokenGeneratorFilter;
import com.jonathanmarquez.security.security.filter.JWTTokenValidatorFilter;
import com.jonathanmarquez.security.security.utils.CookieUtil;
import com.jonathanmarquez.security.security.utils.JwtUtil;
import com.jonathanmarquez.security.utils.ProfileDetector;
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
  private final UserDetailsService userDetailsService;
  private final ProfileDetector profileDetector;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    // 1. Sesión stateless
    http.sessionManagement(session ->
                               session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    // 2. CORS (para desarrollo)
    http.cors(cors -> cors.configurationSource(configureCorsConfigurationSource()));

    // 3. Configuración CSRF
    configureCsrf(http);

    // 4. Configuración de filtros personalizados
    configureCustomFilters(http);


    // 5. Configuración de reglas de autorización
    configureAuthorization(http);


    // 6. configureFormLogin  y HTTP Basic
    configureFormLogin(http);
    configureHttpBasic(http);
    configureExceptionHandling(http);

    return http.build();
  }

  private CorsConfigurationSource configureCorsConfigurationSource() {
    return request -> {
      CorsConfiguration config = new CorsConfiguration();
      config.setAllowedOrigins(List.of("http://localhost:4200", "http://localhost:3000"));
      config.setAllowedMethods(Collections.singletonList("*"));
      config.setAllowCredentials(true);
      config.setAllowedHeaders(Collections.singletonList("*"));
      config.setExposedHeaders(List.of("Authorization", "X-XSRF-TOKEN"));
      config.setMaxAge(3600L);
      return config;
    };
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
            "/api/auth/register",
            "/api/auth/csrf",
            "/api/auth/refresh",
            "/api/auth/public/**",
            "/api/auth/login",
            "/api/auth/logout",
            "/api/auth/verify",
            "/api/auth/resend-otp"
        )
        .csrfTokenRepository(tokenRepository)
    );
  }

  private void configureCustomFilters(HttpSecurity http) {
    http.addFilterBefore(new JWTTokenValidatorFilter(jwtUtil, cookieUtil, userDetailsService), BasicAuthenticationFilter.class);
    http.addFilterAfter(new JWTTokenGeneratorFilter(jwtUtil, cookieUtil), BasicAuthenticationFilter.class);
    http.addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class);
  }

  private void configureAuthorization(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers(
            "/api/auth/register",
            "/api/auth/csrf",
            "/api/auth/refresh",
            "/api/auth/public/**",
            "/api/auth/verify",
            "/api/auth/resend-otp",
            "/error",
            "/api/test/**"
        ).permitAll()
        .requestMatchers("/api/auth/login", "/api/auth/me",  "/api/auth/logout").authenticated()
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
        .anyRequest().authenticated()
    );
  }

  private void configureFormLogin(HttpSecurity http) throws Exception {
    http.formLogin(AbstractHttpConfigurer::disable);
  }


  private void configureHttpBasic(HttpSecurity http) throws Exception {
    http.httpBasic(hbc ->
                       hbc.authenticationEntryPoint(new CustomBasicAuthenticationEntryPoint(profileDetector)));

  }

  private void configureExceptionHandling(HttpSecurity http) throws Exception {
    http.exceptionHandling(ehc ->
                               ehc.accessDeniedHandler(new CustomAccessDeniedHandler(profileDetector)));
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
  }
}