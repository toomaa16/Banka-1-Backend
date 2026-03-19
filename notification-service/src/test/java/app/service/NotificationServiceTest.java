package app.service;

import app.dto.EmailTemplate;
import app.dto.NotificationRequest;
import app.dto.ResolvedEmail;
import app.entities.NotificationType;
import app.template.NotificationTemplateFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link NotificationService}.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private NotificationTemplateFactory templateFactory;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void sendEmailSendsMessage() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));

        notificationService.sendEmail("test@example.com", "Subject", "Body");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void resolveEmailContentDelegatesToResolver() {
        NotificationRequest request = new NotificationRequest("name", "email", Map.of());
        EmailTemplate template = new EmailTemplate("Subject", "Body");
        when(templateFactory.resolve(NotificationType.EMPLOYEE_CREATED)).thenReturn(template);

        ResolvedEmail result = notificationService.resolveEmailContent(request, NotificationType.EMPLOYEE_CREATED);

        assertEquals("email", result.recipientEmail());
        assertEquals("Subject", result.subject());
        assertEquals("Body", result.body());
    }
}
