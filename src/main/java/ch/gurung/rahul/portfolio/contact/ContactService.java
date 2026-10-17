package ch.gurung.rahul.portfolio.contact;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Stores contact messages and mails them to the site owner. The message is saved before mailing, so a mail failure
 * loses nothing: it stays in the database with {@code mail_sent = false}.
 * <p>
 * Logs carry the message id only, never names, addresses or text.
 */
@Service
public class ContactService {

	private static final Logger logger = LoggerFactory.getLogger(ContactService.class);

	private final ContactMessageRepository messages;

	private final JavaMailSender mailSender;

	private final ContactProperties properties;

	private final Clock clock;

	public ContactService(ContactMessageRepository messages, JavaMailSender mailSender, ContactProperties properties,
			Clock clock) {
		this.messages = messages;
		this.mailSender = mailSender;
		this.properties = properties;
		this.clock = clock;
	}

	/**
	 * Saves the message, then tries to mail it. Returns the saved message.
	 */
	public ContactMessage submit(ContactForm form) {
		ContactMessage message = this.messages
			.save(new ContactMessage(form.getName().strip(), form.getEmail().strip(), form.getMessage().strip()));
		try {
			this.mailSender.send(toMail(message));
			message.markMailSent();
			message = this.messages.save(message);
		}
		catch (MailException ex) {
			logger.warn("Contact message {} saved, but sending the mail failed", message.getId(), ex);
		}
		return message;
	}

	/**
	 * Deletes messages older than the retention period, nightly at 03:30.
	 */
	@Scheduled(cron = "0 30 3 * * *")
	public void deleteExpiredMessages() {
		int deleted = this.messages.deleteCreatedBefore(this.clock.instant().minus(this.properties.retention()));
		if (deleted > 0) {
			logger.info("Deleted {} contact messages older than {}", deleted, this.properties.retention());
		}
	}

	private SimpleMailMessage toMail(ContactMessage message) {
		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setFrom(this.properties.effectiveSender());
		mail.setTo(this.properties.recipient());
		mail.setReplyTo(message.getEmail());
		// Line breaks in a header would let a visitor add headers of their own
		mail.setSubject("Portfolio contact: " + message.getName().replaceAll("[\\r\\n]+", " "));
		mail.setText("""
				New message from the contact form on the portfolio website.

				Name:  %s
				Email: %s

				%s
				""".formatted(message.getName(), message.getEmail(), message.getMessage()));
		return mail;
	}

}
