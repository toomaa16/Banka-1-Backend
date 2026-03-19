package app.config;

import app.dto.EmailTemplate;
import app.entities.NotificationType;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * Configuration properties for notification templates and routing keys.
 */
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {

    private Map<String, EmailTemplate> templates;
    private Map<String, NotificationType> routingKeys;

    public Map<String, EmailTemplate> getTemplates() {
        return templates;
    }

    public void setTemplates(Map<String, EmailTemplate> templates) {
        this.templates = templates;
    }

    public Map<String, NotificationType> getRoutingKeys() {
        return routingKeys;
    }

    public void setRoutingKeys(Map<String, NotificationType> routingKeys) {
        this.routingKeys = routingKeys;
    }
}
