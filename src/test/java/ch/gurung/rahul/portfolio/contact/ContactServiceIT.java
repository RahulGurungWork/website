package ch.gurung.rahul.portfolio.contact;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import ch.gurung.rahul.portfolio.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

/**
 * Service against real PostgreSQL with a mocked mail sender (acceptance criterion FR3: email failure keeps the
 * message with {@code mail_sent = false}).
 */
@DataJpaTest
@Import({ ContactService.class, TestcontainersConfiguration.class })
@EnableConfigurationProperties(ContactProperties.class)
class ContactServiceIT {

	static final Instant NOW = Instant.parse("2026-10-17T10:00:00Z");

	@Autowired
	private ContactService service;

	@Autowired
	private ContactMessageRepository messages;

	@Autowired
	private TestEntityManager entityManager;

	@MockitoBean
	private JavaMailSender mailSender;

	@Test
	void storesMessageAndMailsItToRecipient() {
		ContactMessage saved = this.service.submit(form("  Anna Muster\r\nBcc: x@evil.example ", "anna@example.ch"));
		this.entityManager.flush();
		this.entityManager.clear();

		assertThat(this.messages.findById(saved.getId())).hasValueSatisfying((stored) -> {
			assertThat(stored.isMailSent()).isTrue();
			assertThat(stored.getMessage()).isEqualTo("Hallo Rahul");
		});

		ArgumentCaptor<SimpleMailMessage> mail = ArgumentCaptor.forClass(SimpleMailMessage.class);
		then(this.mailSender).should().send(mail.capture());
		assertThat(mail.getValue().getTo()).containsExactly("rahul@gurung.ch");
		assertThat(mail.getValue().getFrom()).isEqualTo("rahul@gurung.ch");
		assertThat(mail.getValue().getReplyTo()).isEqualTo("anna@example.ch");
		assertThat(mail.getValue().getSubject()).isEqualTo("Portfolio contact: Anna Muster Bcc: x@evil.example")
			.doesNotContain("\n", "\r");
		assertThat(mail.getValue().getText()).contains("Hallo Rahul");
	}

	@Test
	void keepsMessageWhenMailFails() {
		willThrow(new MailSendException("SMTP down")).given(this.mailSender).send(any(SimpleMailMessage.class));

		ContactMessage saved = this.service.submit(form("Anna Muster", "anna@example.ch"));
		this.entityManager.flush();
		this.entityManager.clear();

		assertThat(this.messages.findById(saved.getId()))
			.hasValueSatisfying((stored) -> assertThat(stored.isMailSent()).isFalse());
		assertThat(this.messages.findByMailSentFalseOrderByCreatedAtAsc()).extracting(ContactMessage::getId)
			.contains(saved.getId());
	}

	@Test
	void deletesMessagesOlderThanOneYear() {
		insertMessage("Old", "2025-10-16T09:59:59Z");
		insertMessage("Recent", "2025-10-18T00:00:00Z");

		this.service.deleteExpiredMessages();
		this.entityManager.clear();

		assertThat(this.messages.findAll()).extracting(ContactMessage::getName).containsExactly("Recent");
	}

	private void insertMessage(String name, String createdAt) {
		this.entityManager.getEntityManager()
			.createNativeQuery("insert into contact_message (name, email, message, created_at, mail_sent) "
					+ "values (?, 'a@example.ch', 'Text', cast(? as timestamptz), true)")
			.setParameter(1, name)
			.setParameter(2, createdAt)
			.executeUpdate();
	}

	private static ContactForm form(String name, String email) {
		ContactForm form = new ContactForm();
		form.setName(name);
		form.setEmail(email);
		form.setMessage(" Hallo Rahul ");
		return form;
	}

	@TestConfiguration(proxyBeanMethods = false)
	static class FixedClock {

		@Bean
		Clock clock() {
			return Clock.fixed(NOW, ZoneOffset.UTC);
		}

	}

}
