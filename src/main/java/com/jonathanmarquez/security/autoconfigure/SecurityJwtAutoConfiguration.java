package com.jonathanmarquez.security.autoconfigure;

import com.jonathanmarquez.security.email_verification.config.OtpProperties;
import com.jonathanmarquez.security.security.config.SecurityProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration(after = DataSourceAutoConfiguration.class)
@ConditionalOnWebApplication
@EnableConfigurationProperties({
        SecurityProperties.class,
        OtpProperties.class
})
public class SecurityJwtAutoConfiguration {
    // Por ahora vacía — los @Bean vienen en el Paso 4
}