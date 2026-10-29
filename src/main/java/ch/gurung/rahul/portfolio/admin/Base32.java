package ch.gurung.rahul.portfolio.admin;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

/**
 * Base32 (RFC 4648) as authenticator apps expect it for TOTP secrets: upper case, no padding. Decoding ignores
 * spaces, dashes and padding and accepts lower case, because people copy secrets in groups.
 */
public final class Base32 {

	private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

	private Base32() {
	}

	public static String encode(byte[] data) {
		StringBuilder out = new StringBuilder((data.length * 8 + 4) / 5);
		int buffer = 0;
		int bits = 0;
		for (byte b : data) {
			buffer = (buffer << 8) | (b & 0xFF);
			bits += 8;
			while (bits >= 5) {
				out.append(ALPHABET.charAt((buffer >> (bits - 5)) & 0x1F));
				bits -= 5;
			}
		}
		if (bits > 0) {
			out.append(ALPHABET.charAt((buffer << (5 - bits)) & 0x1F));
		}
		return out.toString();
	}

	/**
	 * @throws IllegalArgumentException if the text contains a character outside the Base32 alphabet
	 */
	public static byte[] decode(String text) {
		String clean = text.replaceAll("[\\s=-]", "").toUpperCase(Locale.ROOT);
		ByteArrayOutputStream out = new ByteArrayOutputStream(clean.length() * 5 / 8);
		int buffer = 0;
		int bits = 0;
		for (char c : clean.toCharArray()) {
			int value = ALPHABET.indexOf(c);
			if (value < 0) {
				throw new IllegalArgumentException("Not a Base32 character: " + c);
			}
			buffer = (buffer << 5) | value;
			bits += 5;
			if (bits >= 8) {
				out.write((buffer >> (bits - 8)) & 0xFF);
				bits -= 8;
			}
		}
		return out.toByteArray();
	}

}
