package ch.gurung.rahul.portfolio.contact;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Acceptance criterion FR3: honeypot or too many requests are rejected.
 */
class SpamGuardTest {

	private final RateLimiter rateLimiter = mock(RateLimiter.class);

	private final SpamGuard guard = new SpamGuard(this.rateLimiter);

	@Test
	void acceptsNormalSubmission() {
		given(this.rateLimiter.tryAcquire("203.0.113.1")).willReturn(true);

		assertThat(this.guard.isSpam(form(""), "203.0.113.1")).isFalse();
	}

	@Test
	void rejectsFilledHoneypot() {
		given(this.rateLimiter.tryAcquire("203.0.113.1")).willReturn(true);

		assertThat(this.guard.isSpam(form("https://spam.example"), "203.0.113.1")).isTrue();
	}

	@Test
	void rejectsSubmissionOverRateLimit() {
		given(this.rateLimiter.tryAcquire("203.0.113.1")).willReturn(false);

		assertThat(this.guard.isSpam(form(""), "203.0.113.1")).isTrue();
	}

	private static ContactForm form(String website) {
		ContactForm form = new ContactForm();
		form.setName("Anna Muster");
		form.setEmail("anna@example.ch");
		form.setMessage("Hallo");
		form.setWebsite(website);
		return form;
	}

}
