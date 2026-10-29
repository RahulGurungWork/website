package ch.gurung.rahul.portfolio.admin;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Creates the admin login values for the server's {@code .env} (ADR 0005). Run it on your own computer:
 *
 * <pre>
 * .\mvnw.cmd -q compile exec:java "-Dexec.mainClass=ch.gurung.rahul.portfolio.admin.AdminSetup"
 * </pre>
 *
 * It asks for a username and a password (twice), then prints the three lines for {@code .env} and a link for the
 * authenticator app. Nothing is written to disk.
 */
public final class AdminSetup {

	static final int MIN_PASSWORD_LENGTH = 12;

	static final String ISSUER = "P008 Portfolio";

	private AdminSetup() {
	}

	public static void main(String[] args) throws IOException {
		Input input = Input.open();
		String username = input.line("Username: ").strip();
		if (!username.matches("[A-Za-z0-9._-]{3,100}")) {
			fail("Use 3 to 100 letters, digits, dots, dashes or underscores.");
		}
		char[] password = input.secret("Password (at least " + MIN_PASSWORD_LENGTH + " characters): ");
		char[] repeat = input.secret("Repeat the password: ");
		if (password.length < MIN_PASSWORD_LENGTH) {
			fail("The password is too short.");
		}
		if (!Arrays.equals(password, repeat)) {
			fail("The passwords do not match.");
		}

		String hash = new BCryptPasswordEncoder().encode(new String(password));
		Arrays.fill(password, ' ');
		Arrays.fill(repeat, ' ');
		byte[] key = new byte[20];
		new SecureRandom().nextBytes(key);
		String secret = Base32.encode(key);

		System.out.println();
		System.out.println("Add these lines to ~/portfolio/deploy/.env on the server (keep the single quotes):");
		System.out.println();
		System.out.println("ADMIN_USERNAME=" + username);
		System.out.println("ADMIN_PASSWORD_HASH='" + hash + "'");
		System.out.println("ADMIN_TOTP_SECRET=" + secret);
		System.out.println();
		System.out.println("In your authenticator app, add an account with this key (or open the link):");
		System.out.println("  Key: " + secret.replaceAll("(.{4})(?!$)", "$1 "));
		System.out.println("  " + otpauthLink(username, secret));
		System.out.println();
		System.out.println("Then restart the app: docker compose up -d app");
	}

	/**
	 * Link format understood by authenticator apps (Google Authenticator key URI format).
	 */
	static String otpauthLink(String username, String secret) {
		String label = encode(ISSUER + ":" + username);
		return "otpauth://totp/" + label + "?secret=" + secret + "&issuer=" + encode(ISSUER) + "&digits="
				+ Totp.DIGITS + "&period=" + Totp.STEP_SECONDS;
	}

	private static String encode(String text) {
		return URLEncoder.encode(text, StandardCharsets.UTF_8).replace("+", "%20");
	}

	private static void fail(String message) {
		System.err.println(message);
		System.exit(1);
	}

	/**
	 * Reads from the console without echoing passwords; falls back to standard input when there is no console
	 * (for example inside Maven), where the typed password stays visible.
	 */
	private record Input(Console console, BufferedReader reader) {

		static Input open() {
			Console console = System.console();
			return new Input(console,
					(console != null) ? null : new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8)));
		}

		String line(String prompt) throws IOException {
			if (this.console != null) {
				return orEmpty(this.console.readLine(prompt));
			}
			System.out.print(prompt);
			System.out.flush();
			return orEmpty(this.reader.readLine());
		}

		char[] secret(String prompt) throws IOException {
			if (this.console != null) {
				char[] value = this.console.readPassword(prompt);
				return (value != null) ? value : new char[0];
			}
			return line(prompt).toCharArray();
		}

		private static String orEmpty(String value) {
			return (value != null) ? value : "";
		}

	}

}
