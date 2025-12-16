package com.jonathanmarquez.security.exceptions.customexceptions;

import org.springframework.http.HttpStatus;

public class PasswordPolicyException extends RuntimeException implements CustomErrorResponse {

  public PasswordPolicyException(String message) {
    super(message);
  }

  @Override
  public HttpStatus getStatus() {
    return HttpStatus.BAD_REQUEST;
  }
}