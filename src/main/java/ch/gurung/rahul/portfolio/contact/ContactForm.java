package ch.gurung.rahul.portfolio.contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * What the contact form posts. Messages in braces are keys in {@code i18n/messages*.properties}.
 * {@code website} is the honeypot: hidden from people, so only bots fill it in.
 */
public class ContactForm {

	@NotBlank(message = "{contact.name.required}")
	@Size(max = 100, message = "{contact.name.tooLong}")
	private String name = "";

	@NotBlank(message = "{contact.email.required}")
	@Email(regexp = "[^@\\s]+@[^@\\s]+\\.[^@\\s]+", message = "{contact.email.invalid}")
	@Size(max = 254, message = "{contact.email.invalid}")
	private String email = "";

	@NotBlank(message = "{contact.message.required}")
	@Size(max = 5000, message = "{contact.message.tooLong}")
	private String message = "";

	private String website = "";

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getWebsite() {
		return this.website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

}
