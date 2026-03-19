package app.template;

import app.dto.EmailTemplate;
import app.entities.NotificationType;
import app.exception.BusinessException;
import app.exception.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultNotificationTemplateFactory}.
 */
class DefaultNotificationTemplateFactoryTest {

    @Test
    void resolveReturnsTemplateWhenDefined() {
        Environment environment = mock(Environment.class);
        when(environment.getProperty("notification.templates.EMPLOYEE_CREATED.subject")).thenReturn("Test Subject");
        when(environment.getProperty("notification.templates.EMPLOYEE_CREATED.body")).thenReturn("Test Body");

        DefaultNotificationTemplateFactory factory = new DefaultNotificationTemplateFactory(environment);

        EmailTemplate template = factory.resolve(NotificationType.EMPLOYEE_CREATED);

        assertNotNull(template);
        assertEquals("Test Subject", template.subject());
        assertEquals("Test Body", template.bodyTemplate());
    }

    @Test
    void resolveThrowsWhenTemplateNotDefined() {
        Environment environment = mock(Environment.class);

        DefaultNotificationTemplateFactory factory = new DefaultNotificationTemplateFactory(environment);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> factory.resolve(NotificationType.EMPLOYEE_CREATED));
        assertEquals(ErrorCode.EMAIL_CONTENT_RESOLUTION_FAILED, exception.getErrorCode());
        assertEquals("No template defined for notification type: EMPLOYEE_CREATED", exception.getDetails());
    }
}
