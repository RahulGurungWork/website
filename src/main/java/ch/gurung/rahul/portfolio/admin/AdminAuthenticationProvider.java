package ch.gurung.rahul.portfolio.admin;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Checks username, password and authenticator code together (ADR 0005). All three are checked on every attempt, so
 * the time taken and the error do not reveal which one was wrong. A code is accepted only once.
 */
public class AdminAuthenticationProvider implements AuthenticationProvider {

	static final String ROLE = "ROLE_ADMIN";

	private static final String ACCOUNT_KEY = "account";

	private final AdminProperties properties;

	private final PasswordEncoder passwordEncoder;

	private final Totp totp;

	private final LoginAttemptLimiter ipLimiter;

	private final LoginAttemptLimiter accountLimiter;

	private final String dummyHash;

	private final AtomicLong lastUsedStep = new AtomicLong(Long.MIN_VALUE);

	/**
	 * @param ipLimiter locks one client address after a few failures
	 * @param accountLimiter locks the account after more failures from many addresses; set higher, so one attacker
	 * cannot easily lock the admin out
	 */
	public AdminAuthenticationProvider(AdminProperties properties, PasswordEncoder passwordEncoder, Totp totp,
			LoginAttemptLimiter ipLimiter, LoginAttemptLimiter accountLimiter) {
		this.properties = properties;
		this.passwordEncoder = passwordEncoder;
		this.totp = totp;
		this.ipLimiter = ipLimiter;
		this.accountLimiter = accountLimiter;
		this.dummyHash = passwordEncoder.encode("no admin is configured");
	}

	@Override
	public Authentication authenticate(Authentication authentication) {
		AdminLoginDetails details = (authentication.getDetails() instanceof AdminLoginDetails d) ? d
				: new AdminLoginDetails("unknown", null);
		String ipKey = details.remoteAddress();
		if (this.ipLimiter.isLocked(ipKey) || this.accountLimiter.isLocked(ACCOUNT_KEY)) {
			throw new LockedException("Too many failed logins");
		}
		if (!credentialsMatch(authentication.getName(), String.valueOf(authentication.getCredentials()),
				details.code())) {
			this.ipLimiter.recordFailure(ipKey);
			this.accountLimiter.recordFailure(ACCOUNT_KEY);
			throw new BadCredentialsException("Bad credentials");
		}
		this.ipLimiter.reset(ipKey);
		this.accountLimiter.reset(ACCOUNT_KEY);
		return UsernamePasswordAuthenticationToken.authenticated(authentication.getName(), null,
				List.of(new SimpleGrantedAuthority(ROLE)));
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
	}

	private boolean credentialsMatch(String username, String password, String code) {
		if (!this.properties.configured()) {
			this.passwordEncoder.matches(password, this.dummyHash);
			return false;
		}
		boolean usernameMatches = MessageDigest.isEqual(username.getBytes(StandardCharsets.UTF_8),
				this.properties.username().getBytes(StandardCharsets.UTF_8));
		boolean passwordMatches = this.passwordEncoder.matches(password, this.properties.passwordHash());
		OptionalLong step = this.totp.matchingStep(Base32.decode(this.properties.totpSecret()), code);
		return usernameMatches && passwordMatches && step.isPresent() && markUsed(step.getAsLong());
	}

	/**
	 * Accepts each step at most once, so an observed code cannot be replayed within its 30 seconds.
	 */
	private boolean markUsed(long step) {
		long previous;
		do {
			previous = this.lastUsedStep.get();
			if (step <= previous) {
				return false;
			}
		}
		while (!this.lastUsedStep.compareAndSet(previous, step));
		return true;
	}

}
