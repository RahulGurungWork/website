package ch.gurung.rahul.portfolio.contact;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterTest {

	private final MutableClock clock = new MutableClock(Instant.parse("2026-10-17T10:00:00Z"));

	private final RateLimiter limiter = new RateLimiter(this.clock,
			new ContactProperties("me@example.ch", null, 3, Duration.ofMinutes(10), Duration.ofDays(365)));

	@Test
	void allowsUpToLimitWithinWindow() {
		assertThat(this.limiter.tryAcquire("203.0.113.1")).isTrue();
		assertThat(this.limiter.tryAcquire("203.0.113.1")).isTrue();
		assertThat(this.limiter.tryAcquire("203.0.113.1")).isTrue();
		assertThat(this.limiter.tryAcquire("203.0.113.1")).isFalse();
	}

	@Test
	void countsEachAddressSeparately() {
		for (int i = 0; i < 3; i++) {
			this.limiter.tryAcquire("203.0.113.1");
		}

		assertThat(this.limiter.tryAcquire("203.0.113.2")).isTrue();
	}

	@Test
	void forgetsAttemptsAfterWindow() {
		for (int i = 0; i < 4; i++) {
			this.limiter.tryAcquire("203.0.113.1");
		}
		this.clock.advance(Duration.ofMinutes(10).plusSeconds(1));

		assertThat(this.limiter.tryAcquire("203.0.113.1")).isTrue();
	}

	private static final class MutableClock extends Clock {

		private Instant now;

		MutableClock(Instant now) {
			this.now = now;
		}

		void advance(Duration duration) {
			this.now = this.now.plus(duration);
		}

		@Override
		public Instant instant() {
			return this.now;
		}

		@Override
		public ZoneId getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

	}

}
