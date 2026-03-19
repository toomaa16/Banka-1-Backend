//package app.integration;
//
//import app.dto.NotificationRequest;
//import app.entities.NotificationDelivery;
//import app.entities.NotificationDeliveryStatus;
//import app.entities.NotificationType;
//import app.repository.NotificationDeliveryRepository;
//import com.icegreen.greenmail.util.GreenMail;
//import com.icegreen.greenmail.util.ServerSetup;
//import jakarta.mail.internet.MimeMessage;
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestInstance;
//import org.springframework.amqp.core.*;
//import org.springframework.amqp.rabbit.annotation.EnableRabbit;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.amqp.support.converter.MessageConverter;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.DynamicPropertyRegistry;
//import org.springframework.test.context.DynamicPropertySource;
//import org.testcontainers.containers.RabbitMQContainer;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import java.time.Duration;
//import java.time.Instant;
//import java.util.List;
//import java.util.Map;
//import java.util.function.BooleanSupplier;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.junit.jupiter.api.Assertions.fail;
//
///**
// * Integration test that exercises the real AMQP listener + SMTP send flow.
// *
// * <p>The test publishes a message to RabbitMQ, waits for the listener to process it,
// * verifies DB state transition to SUCCEEDED, and confirms an email was received by SMTP server.
// */
//@Testcontainers(disabledWithoutDocker = true)
//@SpringBootTest(properties = {
//        "spring.rabbitmq.listener.simple.auto-startup=true",
//        "spring.datasource.url=jdbc:h2:mem:notification-it;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
//        "spring.datasource.driver-class-name=org.h2.Driver",
//        "spring.datasource.username=sa",
//        "spring.datasource.password=",
//        "spring.jpa.hibernate.ddl-auto=create-drop",
//        "spring.mail.properties.mail.smtp.auth=false",
//        "spring.mail.properties.mail.smtp.starttls.enable=false",
//        "notification.template.employee_created.subject=Activation Email",
//        "notification.template.employee_created.body=Zdravo {{name}}, vas nalog je kreiran. Aktivirajte nalog klikom na link:\\n{{activationLink}}",
//        "notification.template.employee.password_reset.subject=Password Reset Email",
//        "notification.template.employee.password_reset.body=Zdravo {{name}}, resetujte lozinku klikom na link:\\n{{resetLink}}",
//        "notification.template.employee.account_deactivated.subject=Account Deactivation Email",
//        "notification.template.employee.account_deactivated.body=Zdravo {{name}}, vas nalog je deaktiviran."
//})
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@EnableRabbit
//class NotificationFlowIntegrationTest {
//
//    private static final String TEST_EMAIL = "dimitrije.tomic99@gmail.com";
//
//    private static final RabbitMQContainer RABBIT_MQ = new RabbitMQContainer("rabbitmq:3.13-management");
//
//    private static final GreenMail GREEN_MAIL = new GreenMail(new ServerSetup(3025, "127.0.0.1", "smtp"));
//
//    static {
//        RABBIT_MQ.start();
//        GREEN_MAIL.start();
//    }
//
//    @DynamicPropertySource
//    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.rabbitmq.host", RABBIT_MQ::getHost);
//        registry.add("spring.rabbitmq.port", RABBIT_MQ::getAmqpPort);
//        registry.add("spring.rabbitmq.username", RABBIT_MQ::getAdminUsername);
//        registry.add("spring.rabbitmq.password", RABBIT_MQ::getAdminPassword);
//        registry.add("spring.mail.host", () -> "127.0.0.1");
//        registry.add("spring.mail.port", () -> GREEN_MAIL.getSmtp().getPort());
//        registry.add("notification.template.employee-created.subject", () -> "Activation Email");
//        registry.add("notification.template.employee-created.body", () -> "Zdravo {{name}}, vas nalog je kreiran. Aktivirajte nalog klikom na link:\n{{activationLink}}");
//        registry.add("notification.template.employee-password-reset.subject", () -> "Password Reset Email");
//        registry.add("notification.template.employee-password-reset.body", () -> "Zdravo {{name}}, resetujte lozinku klikom na link:\n{{resetLink}}");
//        registry.add("notification.template.employee-account-deactivated.subject", () -> "Account Deactivation Email");
//        registry.add("notification.template.employee-account-deactivated.body", () -> "Zdravo {{name}}, vas nalog je deaktiviran.");
//    }
//
//    @Autowired
//    private RabbitTemplate rabbitTemplate;
//
//    @Autowired
//    private MessageConverter messageConverter;
//
//    @Autowired
//    private NotificationDeliveryRepository notificationDeliveryRepository;
//
//    @Value("${notification.rabbit.exchange}")
//    private String exchangeName;
//
//    @Autowired
//    private AmqpAdmin amqpAdmin;
//
//    @BeforeEach
//    void clearState() throws Exception {
//        notificationDeliveryRepository.deleteAll();
//        GREEN_MAIL.purgeEmailFromAllMailboxes();
//
//        rabbitTemplate.setMessageConverter(messageConverter);
//
//        Queue queue = new Queue("notification-queue", false, false, true);
//        TopicExchange exchange = new TopicExchange(exchangeName);
//
//        amqpAdmin.declareQueue(queue);
//        amqpAdmin.declareExchange(exchange);
//
//        // bind svi routing key-evi koje testovi koriste
//        amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("EMPLOYEE.CREATED"));
//        amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("EMPLOYEE.PASSWORD_RESET"));
//        amqpAdmin.declareBinding(BindingBuilder.bind(queue).to(exchange).with("EMPLOYEE.ACCOUNT_DEACTIVATED"));
//    }
//
//    @AfterAll
//    void tearDownMailServer() {
//        GREEN_MAIL.stop();
//        RABBIT_MQ.stop();
//    }
//
//    @Test
//    void messageFromRabbitMqIsConsumedTypeResolvedAndEmailSent() throws Exception {
//        NotificationRequest request = new NotificationRequest(
//                "Dimitrije",
//                TEST_EMAIL,
//                Map.of(
//                        "name", "Dimitrije",
//                        "activationLink", "https://example.com/activate/123"
//                )
//        );
//
//        rabbitTemplate.convertAndSend(exchangeName, "EMPLOYEE.CREATED", request);
//
//        waitForCondition(
//                Duration.ofSeconds(15),
//                () -> !notificationDeliveryRepository.findAllByStatus(NotificationDeliveryStatus.SUCCEEDED).isEmpty()
//                        && GREEN_MAIL.getReceivedMessages().length == 1
//        );
//
//        List<NotificationDelivery> succeeded = notificationDeliveryRepository
//                .findAllByStatus(NotificationDeliveryStatus.SUCCEEDED);
//        assertEquals(1, succeeded.size());
//
//        NotificationDelivery delivery = succeeded.get(0);
//        assertEquals(NotificationType.EMPLOYEE_CREATED, delivery.getNotificationType());
//        assertEquals(TEST_EMAIL, delivery.getRecipientEmail());
//        assertEquals("Activation Email", delivery.getSubject());
//        assertNotNull(delivery.getSentAt());
//        assertEquals(0, delivery.getRetryCount());
//
//        MimeMessage[] messages = GREEN_MAIL.getReceivedMessages();
//        assertEquals(1, messages.length);
//        assertEquals("Activation Email", messages[0].getSubject());
//        assertEquals(TEST_EMAIL, messages[0].getAllRecipients()[0].toString());
//    }
//
//    @Test
//    void passwordResetEventIsConsumedAndEmailSent() throws Exception {
//        NotificationRequest request = new NotificationRequest(
//                "Dimitrije",
//                TEST_EMAIL,
//                Map.of(
//                        "name", "Dimitrije",
//                        "resetLink", "https://example.com/reset/abc"
//                )
//        );
//
//        rabbitTemplate.convertAndSend(exchangeName, "EMPLOYEE.PASSWORD_RESET", request);
//
//        waitForCondition(
//                Duration.ofSeconds(15),
//                () -> !notificationDeliveryRepository.findAllByStatus(NotificationDeliveryStatus.SUCCEEDED).isEmpty()
//                        && GREEN_MAIL.getReceivedMessages().length == 1
//        );
//
//        List<NotificationDelivery> succeeded = notificationDeliveryRepository
//                .findAllByStatus(NotificationDeliveryStatus.SUCCEEDED);
//        assertEquals(1, succeeded.size());
//
//        NotificationDelivery delivery = succeeded.get(0);
//        assertEquals(NotificationType.EMPLOYEE_PASSWORD_RESET, delivery.getNotificationType());
//        assertEquals(TEST_EMAIL, delivery.getRecipientEmail());
//        assertEquals("Password Reset Email", delivery.getSubject());
//        assertNotNull(delivery.getSentAt());
//
//        MimeMessage[] messages = GREEN_MAIL.getReceivedMessages();
//        assertEquals(1, messages.length);
//        assertEquals("Password Reset Email", messages[0].getSubject());
//    }
//
//    @Test
//    void accountDeactivatedEventIsConsumedAndEmailSent() throws Exception {
//        NotificationRequest request = new NotificationRequest(
//                "Dimitrije",
//                TEST_EMAIL,
//                Map.of("name", "Dimitrije")
//        );
//
//        rabbitTemplate.convertAndSend(exchangeName, "EMPLOYEE.ACCOUNT_DEACTIVATED", request);
//
//        waitForCondition(
//                Duration.ofSeconds(15),
//                () -> !notificationDeliveryRepository.findAllByStatus(NotificationDeliveryStatus.SUCCEEDED).isEmpty()
//                        && GREEN_MAIL.getReceivedMessages().length == 1
//        );
//
//        List<NotificationDelivery> succeeded = notificationDeliveryRepository
//                .findAllByStatus(NotificationDeliveryStatus.SUCCEEDED);
//        assertEquals(1, succeeded.size());
//
//        NotificationDelivery delivery = succeeded.get(0);
//        assertEquals(NotificationType.EMPLOYEE_ACCOUNT_DEACTIVATED, delivery.getNotificationType());
//        assertEquals(TEST_EMAIL, delivery.getRecipientEmail());
//        assertEquals("Account Deactivation Email", delivery.getSubject());
//        assertNotNull(delivery.getSentAt());
//
//        MimeMessage[] messages = GREEN_MAIL.getReceivedMessages();
//        assertEquals(1, messages.length);
//        assertEquals("Account Deactivation Email", messages[0].getSubject());
//    }
//
//    @Test
//    void unknownRoutingKeyIsDroppedAndPersistedAsFailed() {
//        NotificationRequest request = new NotificationRequest(
//                "Dimitrije",
//                TEST_EMAIL,
//                Map.of()
//        );
//
//        rabbitTemplate.convertAndSend(exchangeName, "employee.unknown", request);
//
//        waitForCondition(
//                Duration.ofSeconds(15),
//                () -> !notificationDeliveryRepository.findAllByStatus(NotificationDeliveryStatus.FAILED).isEmpty()
//        );
//
//        List<NotificationDelivery> failed = notificationDeliveryRepository
//                .findAllByStatus(NotificationDeliveryStatus.FAILED);
//        assertEquals(1, failed.size());
//
//        NotificationDelivery delivery = failed.get(0);
//        assertEquals(NotificationType.UNKNOWN, delivery.getNotificationType());
//        assertTrue(delivery.getLastError().contains("employee.unknown_event"));
//        assertEquals(0, GREEN_MAIL.getReceivedMessages().length);
//    }
//
//    private void waitForCondition(Duration timeout, BooleanSupplier condition) {
//        Instant deadline = Instant.now().plus(timeout);
//        while (Instant.now().isBefore(deadline)) {
//            if (condition.getAsBoolean()) {
//                return;
//            }
//            try {
//                Thread.sleep(200);
//            } catch (InterruptedException ex) {
//                Thread.currentThread().interrupt();
//                fail("Interrupted while waiting for async processing");
//            }
//        }
//        fail("Condition not met within timeout: " + timeout);
//    }
//}
