package ch.gurung.rahul.portfolio.admin;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Settings under {@code portfolio.admin}, filled from the server's environment (ADR 0005). Generate the values with
 * {@link AdminSetup}.
 *
 * @param username admin login name
 * @param passwordHash BCrypt hash of the admin password
 * @param totpSecret Base32 secret shared with the authenticator app
 */
@ConfigurationProperties("portfolio.admin")
public record AdminProperties(String username, String passwordHash, String totpSecret) {

	/**
	 * Whether all three values are set. Without them nobody can log in, but the public site runs normally.
	 */
	public boolean configured() {
		return hasText(this.username) && hasText(this.passwordHash) && hasText(this.totpSecret);
	}

	private static boolean hasText(String value) {
		return value != null && !value.isBlank();
	}

}
