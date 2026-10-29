package ch.gurung.rahul.portfolio.admin;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class Base32Test {

	private static final byte[] RFC_KEY = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);

	@Test
	void encodesWithoutPadding() {
		assertThat(Base32.encode(RFC_KEY)).isEqualTo("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ");
		assertThat(Base32.encode("f".getBytes(StandardCharsets.US_ASCII))).isEqualTo("MY");
	}

	@Test
	void decodesWhatPeopleCopy() {
		assertThat(Base32.decode("gezd gnbv gy3t qojq gezd gnbv gy3t qojq")).isEqualTo(RFC_KEY);
		assertThat(Base32.decode("MY======")).isEqualTo("f".getBytes(StandardCharsets.US_ASCII));
	}

	@Test
	void rejectsOtherCharacters() {
		assertThatIllegalArgumentException().isThrownBy(() -> Base32.decode("GEZD1"));
	}

}
