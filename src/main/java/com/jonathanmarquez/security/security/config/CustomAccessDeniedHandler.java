package com.jonathanmarquez.security.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jonathanmarquez.security.exceptions.helpers.ErrorApiResponseHelper;
import com.jonathanmarquez.security.exceptions.response.ErrorApiResponse;
import com.jonathanmarquez.security.utils.ProfileDetector;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  private final ProfileDetector profileDetector;
  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
                     AccessDeniedException accessDeniedException) throws IOException, ServletException {
    HttpStatus status = HttpStatus.FORBIDDEN;
    String path = request.getRequestURI();
    String message = "No tiene permisos para acceder a este recurso";

    ErrorApiResponse errorResponse = ErrorApiResponse.builder()
                                                     .success(false)
                                                     .message(message)
                                                     .status(status.getReasonPhrase().toLowerCase())
                                                     .statusCode(status.value())
                                                     .timestamp(LocalDateTime.now())
                                                     .path(path)
                                                     .details(ErrorApiResponseHelper.buildDetails(profileDetector, accessDeniedException, null))
                                                                                    .build();

    response.setHeader("denied-reason", "Authorization failed");
    response.setStatus(status.value());
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}
