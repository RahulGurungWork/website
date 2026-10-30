package ch.gurung.rahul.portfolio.contact;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * A message sent through the contact form. Stored before mailing, so a mail failure loses nothing.
 */
@Entity
public class ContactMessage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private String email;

	private String message;

	private Instant createdAt;

	private boolean mailSent;

	/**
	 * When the admin first opened the message; {@code null} while unread (step A4).
	 */
	private Instant readAt;

	protected ContactMessage() {
	}

	public ContactMessage(String name, String email, String message) {
		this.name = name;
		this.email = email;
		this.message = message;
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getEmail() {
		return this.email;
	}

	public String getMessage() {
		return this.message;
	}

	public Instant getCreatedAt() {
		return this.createdAt;
	}

	public boolean isMailSent() {
		return this.mailSent;
	}

	public void markMailSent() {
		this.mailSent = true;
	}

	public Instant getReadAt() {
		return this.readAt;
	}

	public boolean isRead() {
		return this.readAt != null;
	}

	/**
	 * Keeps the time of the first reading.
	 */
	void markRead(Instant now) {
		if (this.readAt == null) {
			this.readAt = now;
		}
	}

	void markUnread() {
		this.readAt = null;
	}

}
