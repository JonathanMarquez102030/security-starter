package com.jonathanmarquez.security.exceptions.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class ErrorApiResponse extends ApiResponse<Void> {

  private String details;
}
