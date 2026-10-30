package ch.gurung.rahul.portfolio.contact;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The contact form inbox for the admin panel (step A4). Messages are still deleted after a year by
 * {@link ContactService} (privacy notice); the admin can delete them earlier.
 */
@Service
@Transactional
public class MessageAdminService {

	/**
	 * Times are shown in Swiss time, whatever the server's time zone.
	 */
	static final ZoneId ZONE = ZoneId.of("Europe/Zurich");

	static final int PREVIEW_LENGTH = 80;

	private final ContactMessageRepository messages;

	private final Clock clock;

	public MessageAdminService(ContactMessageRepository messages, Clock clock) {
		this.messages = messages;
		this.clock = clock;
	}

	/**
	 * One row of the inbox.
	 *
	 * @param preview the start of the message on one line
	 * @param mailSent whether the notification email reached the mail server
	 */
	public record Row(long id, String name, ZonedDateTime receivedAt, String preview, boolean read,
			boolean mailSent) {
	}

	/**
	 * One message in full.
	 */
	public record Detail(long id, String name, String email, String message, ZonedDateTime receivedAt,
			boolean mailSent) {
	}

	@Transactional(readOnly = true)
	public List<Row> list() {
		return this.messages.findAllByOrderByCreatedAtDescIdDesc()
			.stream()
			.map((m) -> new Row(m.getId(), m.getName(), m.getCreatedAt().atZone(ZONE), preview(m.getMessage()),
					m.isRead(), m.isMailSent()))
			.toList();
	}

	@Transactional(readOnly = true)
	public long unreadCount() {
		return this.messages.countByReadAtIsNull();
	}

	/**
	 * The message, marked as read; empty if it does not exist.
	 */
	public Optional<Detail> open(long id) {
		return this.messages.findById(id).map((m) -> {
			m.markRead(this.clock.instant());
			return new Detail(m.getId(), m.getName(), m.getEmail(), m.getMessage(), m.getCreatedAt().atZone(ZONE),
					m.isMailSent());
		});
	}

	public boolean markUnread(long id) {
		Optional<ContactMessage> message = this.messages.findById(id);
		message.ifPresent(ContactMessage::markUnread);
		return message.isPresent();
	}

	@Transactional(readOnly = true)
	public Optional<String> sender(long id) {
		return this.messages.findById(id).map(ContactMessage::getName);
	}

	public void delete(long id) {
		this.messages.findById(id).ifPresent(this.messages::delete);
	}

	private static String preview(String message) {
		String oneLine = message.replaceAll("\\s+", " ").strip();
		return (oneLine.length() <= PREVIEW_LENGTH) ? oneLine : oneLine.substring(0, PREVIEW_LENGTH - 1) + "…";
	}

}
