package com.jonathanmarquez.security.exceptions.response;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class ApiResponseFactory {

  private final HttpServletRequest request;

  public <T> ResponseEntity<SuccessApiResponse<T>> ok(String message, T data) {
    return build(HttpStatus.OK, message, data);
  }

  public <T> ResponseEntity<SuccessApiResponse<T>> created(String message, T data) {
    return build(HttpStatus.CREATED, message, data);
  }

  public ResponseEntity<SuccessApiResponse<Void>> ok(String message) {
    return build(HttpStatus.OK, message, null);
  }

  public <T> ResponseEntity<SuccessApiResponse<T>> build(
      HttpStatus status,
      String message,
      T data
  ) {
    SuccessApiResponse<T> body = SuccessApiResponse.<T>builder()
                                                   .success(true)
                                                   .message(message)
                                                   .status(status.getReasonPhrase())
                                                   .statusCode(status.value())
                                                   .timestamp(LocalDateTime.now())
                                                   .path(request.getRequestURI())
                                                   .data(data)
                                                   .build();

    return ResponseEntity.status(status).body(body);
  }
}