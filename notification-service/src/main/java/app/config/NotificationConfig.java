package app.config;

import app.dto.EmailTemplate;
import app.entities.NotificationType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for notification templates and routing keys loaded from properties.
 */
@Configuration
public class NotificationConfig {

    @Bean
    @ConfigurationProperties(prefix = "notification.templates")
    public Map<String, EmailTemplate> emailTemplates() {
        return new HashMap<>();
    }

    @Bean
    @ConfigurationProperties(prefix = "notification.routing-keys")
    public Map<String, NotificationType> routingKeys() {
        return new HashMap<>();
    }
}
