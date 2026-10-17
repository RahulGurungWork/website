package ch.gurung.rahul.portfolio.contact;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Settings under {@code portfolio.contact}.
 *
 * @param recipient mailbox that receives contact messages
 * @param sender From address of those mails; defaults to {@code recipient}, because the SMTP provider only accepts
 * the authenticated mailbox as sender
 * @param maxPerWindow submissions allowed per IP address within {@code window}
 * @param window rate-limit window
 * @param retention how long messages are kept before the nightly cleanup deletes them (see the privacy notice)
 */
@ConfigurationProperties("portfolio.contact")
public record ContactProperties(String recipient, String sender, @DefaultValue("5") int maxPerWindow,
		@DefaultValue("10m") Duration window, @DefaultValue("365d") Duration retention) {

	public String effectiveSender() {
		return (this.sender != null && !this.sender.isBlank()) ? this.sender : this.recipient;
	}

}
