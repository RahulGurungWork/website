package ch.gurung.rahul.portfolio.admin;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptLimiterTest {

	private final MutableClock clock = new MutableClock(Instant.parse("2026-10-29T12:00:00Z"));

	private final LoginAttemptLimiter limiter = new LoginAttemptLimiter(this.clock, 5);

	@Test
	void locksAfterFiveFailuresWithinWindow() {
		failTimes("10.0.0.1", 4);
		assertThat(this.limiter.isLocked("10.0.0.1")).isFalse();

		failTimes("10.0.0.1", 1);
		assertThat(this.limiter.isLocked("10.0.0.1")).isTrue();
		assertThat(this.limiter.isLocked("10.0.0.2")).isFalse();
	}

	@Test
	void unlocksAfterLockPeriod() {
		failTimes("10.0.0.1", 5);

		this.clock.advance(LoginAttemptLimiter.LOCK.minusSeconds(1));
		assertThat(this.limiter.isLocked("10.0.0.1")).isTrue();
		this.clock.advance(Duration.ofSeconds(1));
		assertThat(this.limiter.isLocked("10.0.0.1")).isFalse();
	}

	@Test
	void forgetsFailuresOutsideWindow() {
		failTimes("10.0.0.1", 4);
		this.clock.advance(LoginAttemptLimiter.WINDOW);

		failTimes("10.0.0.1", 1);
		assertThat(this.limiter.isLocked("10.0.0.1")).isFalse();
	}

	@Test
	void resetClearsFailures() {
		failTimes("10.0.0.1", 4);
		this.limiter.reset("10.0.0.1");

		failTimes("10.0.0.1", 1);
		assertThat(this.limiter.isLocked("10.0.0.1")).isFalse();
	}

	private void failTimes(String key, int times) {
		for (int i = 0; i < times; i++) {
			this.limiter.recordFailure(key);
		}
	}

}
