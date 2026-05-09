/*
 * Copyright (c) 2026 Jonathan Márquez Pérez.
 * Licensed under the MIT License. See LICENSE file in the project root for full license information.
 */
package com.jonathanmarquez.security.autoconfigure;

import com.jonathanmarquez.security.exceptions.GlobalExceptionHandler;
import com.jonathanmarquez.security.utils.ProfileDetector;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = SecurityCoreAutoConfiguration.class)
@ConditionalOnWebApplication
public class ExceptionHandlingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler(ProfileDetector profileDetector) {
        return new GlobalExceptionHandler(profileDetector);
    }
}
