package app.template;

import app.dto.EmailTemplate;
import app.entities.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link DefaultNotificationTemplateFactory}.
 */
class DefaultNotificationTemplateFactoryUnitTest {

    private DefaultNotificationTemplateFactory factory;

    @BeforeEach
    void setUp() {
        Map<String, EmailTemplate> templates = Map.of(
                "EMPLOYEE_CREATED", new EmailTemplate(
                        "Activation Email",
                        "Zdravo {{name}}, vas nalog je kreiran. Aktivirajte nalog klikom na link:\n{{activationLink}}"
                ),
                "EMPLOYEE_PASSWORD_RESET", new EmailTemplate(
                        "Password Reset Email",
                        "Zdravo {{name}}, resetujte lozinku klikom na link:\n{{resetLink}}"
                ),
                "EMPLOYEE_ACCOUNT_DEACTIVATED", new EmailTemplate(
                        "Account Deactivation Email",
                        "Zdravo {{name}}, vas nalog je deaktiviran."
                )
        );

        factory = new DefaultNotificationTemplateFactory(templates);
    }

    @Test
    void resolveEmployeeCreatedReturnsActivationTemplate() {
        EmailTemplate template = factory.resolve(NotificationType.EMPLOYEE_CREATED);

        assertEquals("Activation Email", template.subject());
        assertTrue(template.bodyTemplate().contains("{{activationLink}}"));
        assertTrue(template.bodyTemplate().contains("{{name}}"));
    }

    @Test
    void resolveEmployeePasswordResetReturnsResetTemplate() {
        EmailTemplate template = factory.resolve(NotificationType.EMPLOYEE_PASSWORD_RESET);

        assertEquals("Password Reset Email", template.subject());
        assertTrue(template.bodyTemplate().contains("{{resetLink}}"));
        assertTrue(template.bodyTemplate().contains("{{name}}"));
    }

    @Test
    void resolveEmployeeAccountDeactivatedReturnsDeactivationTemplate() {
        EmailTemplate template = factory.resolve(NotificationType.EMPLOYEE_ACCOUNT_DEACTIVATED);

        assertEquals("Account Deactivation Email", template.subject());
        assertTrue(template.bodyTemplate().contains("{{name}}"));
    }

    @Test
    void resolveUnknownNotificationTypeThrows() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> factory.resolve(NotificationType.UNKNOWN)
        );
        assertTrue(ex.getMessage().contains("UNKNOWN"));
    }

    @Test
    void allSupportedTypesReturnDistinctSubjects() {
        EmailTemplate created = factory.resolve(NotificationType.EMPLOYEE_CREATED);
        EmailTemplate reset = factory.resolve(NotificationType.EMPLOYEE_PASSWORD_RESET);
        EmailTemplate deactivated = factory.resolve(NotificationType.EMPLOYEE_ACCOUNT_DEACTIVATED);

        assertNotEquals(created.subject(), reset.subject());
        assertNotEquals(created.subject(), deactivated.subject());
        assertNotEquals(reset.subject(), deactivated.subject());
    }
}
