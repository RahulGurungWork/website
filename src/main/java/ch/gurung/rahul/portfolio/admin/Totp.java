package ch.gurung.rahul.portfolio.admin;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.OptionalLong;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Time-based one-time passwords (RFC 6238) as authenticator apps produce them: HMAC-SHA1, 30-second steps, 6 digits.
 */
public class Totp {

	static final long STEP_SECONDS = 30;

	static final int DIGITS = 6;

	/**
	 * Steps accepted before and after the current one, for clocks that are slightly off.
	 */
	static final int ALLOWED_DRIFT = 1;

	private final Clock clock;

	public Totp(Clock clock) {
		this.clock = clock;
	}

	/**
	 * The step at the clock's current time.
	 */
	public long currentStep() {
		return this.clock.instant().getEpochSecond() / STEP_SECONDS;
	}

	/**
	 * The step whose code equals {@code code}, within {@link #ALLOWED_DRIFT} of now; empty if none matches or the
	 * code is not six digits.
	 */
	public OptionalLong matchingStep(byte[] key, String code) {
		if (code == null || !code.matches("\\d{" + DIGITS + "}")) {
			return OptionalLong.empty();
		}
		byte[] given = code.getBytes(StandardCharsets.US_ASCII);
		long now = currentStep();
		for (long step = now - ALLOWED_DRIFT; step <= now + ALLOWED_DRIFT; step++) {
			byte[] expected = codeAt(key, step).getBytes(StandardCharsets.US_ASCII);
			if (MessageDigest.isEqual(expected, given)) {
				return OptionalLong.of(step);
			}
		}
		return OptionalLong.empty();
	}

	/**
	 * The 6-digit code for {@code step} (RFC 4226 dynamic truncation).
	 */
	public static String codeAt(byte[] key, long step) {
		try {
			Mac mac = Mac.getInstance("HmacSHA1");
			mac.init(new SecretKeySpec(key, "HmacSHA1"));
			byte[] hash = mac.doFinal(ByteBuffer.allocate(Long.BYTES).putLong(step).array());
			int offset = hash[hash.length - 1] & 0x0F;
			int binary = ((hash[offset] & 0x7F) << 24) | ((hash[offset + 1] & 0xFF) << 16)
					| ((hash[offset + 2] & 0xFF) << 8) | (hash[offset + 3] & 0xFF);
			return String.format("%0" + DIGITS + "d", binary % 1_000_000);
		}
		catch (GeneralSecurityException ex) {
			throw new IllegalStateException("HmacSHA1 is not available", ex);
		}
	}

}
