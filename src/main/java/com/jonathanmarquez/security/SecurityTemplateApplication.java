package com.jonathanmarquez.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SecurityTemplateApplication {

  public static void main(String[] args) {
    SpringApplication.run(SecurityTemplateApplication.class, args);
  }

}
