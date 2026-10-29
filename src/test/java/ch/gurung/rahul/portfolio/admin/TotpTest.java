package ch.gurung.rahul.portfolio.admin;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Codes must match what authenticator apps show: the SHA-1 test vectors of RFC 6238, Appendix B (last six of the
 * eight digits).
 */
class TotpTest {

	private static final byte[] RFC_KEY = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);

	@ParameterizedTest
	@CsvSource({ "59, 287082", "1111111109, 081804", "1111111111, 050471", "1234567890, 005924",
			"2000000000, 279037", "20000000000, 353130" })
	void matchesRfc6238TestVectors(long epochSeconds, String code) {
		assertThat(Totp.codeAt(RFC_KEY, epochSeconds / Totp.STEP_SECONDS)).isEqualTo(code);
	}

	@Test
	void acceptsCurrentAndNeighbouringStepsOnly() {
		Totp totp = new Totp(new MutableClock(Instant.ofEpochSecond(1111111111)));
		long now = 1111111111 / Totp.STEP_SECONDS;

		assertThat(totp.matchingStep(RFC_KEY, "050471")).hasValue(now);
		assertThat(totp.matchingStep(RFC_KEY, Totp.codeAt(RFC_KEY, now - 1))).hasValue(now - 1);
		assertThat(totp.matchingStep(RFC_KEY, Totp.codeAt(RFC_KEY, now + 1))).hasValue(now + 1);
		assertThat(totp.matchingStep(RFC_KEY, Totp.codeAt(RFC_KEY, now - 2))).isEmpty();
	}

	@Test
	void rejectsMalformedCodes() {
		Totp totp = new Totp(new MutableClock(Instant.ofEpochSecond(59)));

		assertThat(totp.matchingStep(RFC_KEY, null)).isEmpty();
		assertThat(totp.matchingStep(RFC_KEY, "28708")).isEmpty();
		assertThat(totp.matchingStep(RFC_KEY, "2870820")).isEmpty();
		assertThat(totp.matchingStep(RFC_KEY, "28708a")).isEmpty();
	}

}
