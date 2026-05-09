/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquezperez.security.security.config;

import org.jspecify.annotations.NonNull;
import tools.jackson.databind.ObjectMapper;
import com.jonathanmarquezperez.security.exceptions.helpers.ErrorApiResponseHelper;
import com.jonathanmarquezperez.security.exceptions.response.ErrorApiResponse;
import com.jonathanmarquezperez.security.utils.ProfileDetector;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Manejador personalizado para excepciones de acceso denegado en Spring Security.
 *
 * <p>Esta clase intercepta las excepciones de tipo {@link AccessDeniedException} que ocurren
 * cuando un usuario autenticado intenta acceder a recursos para los cuales no tiene permisos
 * suficientes. Genera una respuesta JSON estructurada con código HTTP 403 (Forbidden) y
 * detalles del error según el perfil de ejecución activo.</p>
 */
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  private final ProfileDetector profileDetector;
  private final ObjectMapper objectMapper;

  /**
   * Maneja las excepciones de acceso denegado generando una respuesta JSON con el error.
   *
   * <p>Construye una respuesta de error estructurada que incluye el código de estado 403,
   * un mensaje descriptivo, la ruta solicitada, timestamp y detalles adicionales según el
   * perfil activo. Establece encabezados HTTP apropiados y escribe la respuesta en formato
   * JSON con codificación UTF-8.</p>
   *
   * @param request               solicitud HTTP que generó la excepción de acceso denegado
   * @param response              respuesta HTTP donde se escribirá el error en formato JSON
   * @param accessDeniedException excepción lanzada cuando el acceso es denegado
   * @throws IOException si ocurre un error al escribir la respuesta
   */
  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
                     @NonNull AccessDeniedException accessDeniedException) throws IOException {
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
                                                     .details(ErrorApiResponseHelper.buildDetails(profileDetector,
                                                                                                  accessDeniedException,
                                                                                                  null))
                                                     .build();

    response.setHeader("denied-reason", "Authorization failed");
    response.setStatus(status.value());
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}
