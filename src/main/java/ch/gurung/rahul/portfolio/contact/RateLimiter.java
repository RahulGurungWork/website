package ch.gurung.rahul.portfolio.contact;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Sliding-window limit on contact submissions per IP address. Kept in memory only and forgotten after the window
 * (privacy notice: IP addresses are not stored), so a restart resets it.
 */
@Component
public class RateLimiter {

	private final Clock clock;

	private final int maxPerWindow;

	private final Duration window;

	private final Map<String, Deque<Instant>> attempts = new ConcurrentHashMap<>();

	public RateLimiter(Clock clock, ContactProperties properties) {
		this.clock = clock;
		this.maxPerWindow = properties.maxPerWindow();
		this.window = properties.window();
	}

	/**
	 * Records an attempt from {@code ip} and returns whether it is within the limit.
	 */
	public boolean tryAcquire(String ip) {
		Instant now = this.clock.instant();
		Instant cutoff = now.minus(this.window);
		removeExpired(cutoff);
		Deque<Instant> recent = this.attempts.computeIfAbsent(ip, (key) -> new ArrayDeque<>());
		synchronized (recent) {
			recent.addLast(now);
			return recent.size() <= this.maxPerWindow;
		}
	}

	private void removeExpired(Instant cutoff) {
		this.attempts.values().removeIf((recent) -> {
			synchronized (recent) {
				while (!recent.isEmpty() && !recent.peekFirst().isAfter(cutoff)) {
					recent.removeFirst();
				}
				return recent.isEmpty();
			}
		});
	}

}
