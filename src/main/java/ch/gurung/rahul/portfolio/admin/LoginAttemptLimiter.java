package ch.gurung.rahul.portfolio.admin;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Locks a key (a client IP, or the admin account) after too many failed logins (ADR 0005): {@code maxFailures}
 * failures within {@link #WINDOW} lock it for {@link #LOCK}. Kept in memory only, like the contact rate limit.
 */
public class LoginAttemptLimiter {

	static final Duration WINDOW = Duration.ofMinutes(15);

	static final Duration LOCK = Duration.ofMinutes(15);

	private final Clock clock;

	private final int maxFailures;

	private final Map<String, Deque<Instant>> failures = new ConcurrentHashMap<>();

	private final Map<String, Instant> lockedUntil = new ConcurrentHashMap<>();

	public LoginAttemptLimiter(Clock clock, int maxFailures) {
		this.clock = clock;
		this.maxFailures = maxFailures;
	}

	public boolean isLocked(String key) {
		Instant until = this.lockedUntil.get(key);
		if (until == null) {
			return false;
		}
		if (this.clock.instant().isBefore(until)) {
			return true;
		}
		this.lockedUntil.remove(key, until);
		return false;
	}

	public void recordFailure(String key) {
		Instant now = this.clock.instant();
		Deque<Instant> recent = this.failures.computeIfAbsent(key, (k) -> new ArrayDeque<>());
		synchronized (recent) {
			while (!recent.isEmpty() && !recent.peekFirst().isAfter(now.minus(WINDOW))) {
				recent.removeFirst();
			}
			recent.addLast(now);
			if (recent.size() >= this.maxFailures) {
				this.lockedUntil.put(key, now.plus(LOCK));
				recent.clear();
			}
		}
	}

	public void reset(String key) {
		this.failures.remove(key);
		this.lockedUntil.remove(key);
	}

}
