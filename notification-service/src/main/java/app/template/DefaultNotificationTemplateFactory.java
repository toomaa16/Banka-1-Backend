package app.template;

import app.dto.EmailTemplate;
import app.entities.NotificationType;
import app.exception.BusinessException;
import app.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Default mapping from event type to email subject and template.
 */
@Component
public final class DefaultNotificationTemplateFactory implements NotificationTemplateFactory {
    /**
     * Resolves email template based on notification type.
     *
     * @param type notification event type
     * @return email template containing subject and body
     */

    private final Map<String, EmailTemplate> templates;

    @Autowired
    public DefaultNotificationTemplateFactory(Environment environment) {
        this.templates = loadTemplates(environment);
    }
    public DefaultNotificationTemplateFactory(Map<String, EmailTemplate> templates) {
        this.templates = templates;
    }

    private Map<String, EmailTemplate> loadTemplates(Environment environment) {
        Map<String, EmailTemplate> map = new HashMap<>();
        // Load from properties
        for (NotificationType type : NotificationType.values()) {
            String prefix = "notification.templates." + type.name();
            String subject = environment.getProperty(prefix + ".subject");
            String body = environment.getProperty(prefix + ".body");
            if (subject != null && body != null) {
                map.put(type.name(), new EmailTemplate(subject, body));
            }
        }
        return map;
    }

    @Override
    public EmailTemplate resolve(NotificationType type) {

        EmailTemplate template = templates.get(type.name());

        if (template == null) {
            throw new BusinessException(ErrorCode.EMAIL_CONTENT_RESOLUTION_FAILED, "No template defined for notification type: " + type);
        }

        return template;
    }
}
