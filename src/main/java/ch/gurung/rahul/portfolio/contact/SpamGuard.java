package ch.gurung.rahul.portfolio.contact;

import org.springframework.stereotype.Component;

/**
 * Decides whether a submission is spam: a filled-in honeypot, or too many submissions from one IP address. The
 * controller answers spam with the normal confirmation, so bots learn nothing.
 */
@Component
public class SpamGuard {

	private final RateLimiter rateLimiter;

	public SpamGuard(RateLimiter rateLimiter) {
		this.rateLimiter = rateLimiter;
	}

	public boolean isSpam(ContactForm form, String ip) {
		boolean honeypotFilled = form.getWebsite() != null && !form.getWebsite().isBlank();
		boolean overLimit = !this.rateLimiter.tryAcquire(ip);
		return honeypotFilled || overLimit;
	}

}
