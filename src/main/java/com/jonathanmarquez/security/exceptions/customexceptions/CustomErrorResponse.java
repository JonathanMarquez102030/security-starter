package com.jonathanmarquez.security.exceptions.customexceptions;

import org.springframework.http.HttpStatus;

public interface CustomErrorResponse {

  /**
   * Return the HTTP status to use for the response.
   */
  HttpStatus getStatus();
}
