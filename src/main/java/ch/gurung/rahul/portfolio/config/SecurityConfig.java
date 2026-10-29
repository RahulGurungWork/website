package ch.gurung.rahul.portfolio.config;

import java.io.IOException;
import java.time.Clock;

import ch.gurung.rahul.portfolio.admin.AdminAuthenticationProvider;
import ch.gurung.rahul.portfolio.admin.AdminLoginDetails;
import ch.gurung.rahul.portfolio.admin.AdminProperties;
import ch.gurung.rahul.portfolio.admin.LoginAttemptLimiter;
import ch.gurung.rahul.portfolio.admin.Totp;
import ch.gurung.rahul.portfolio.common.Languages;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Two filter chains (ADR 0005). The admin chain covers {@code /en/admin}, {@code /de/admin} and the login and logout
 * endpoints under {@code /admin}: login with password and authenticator code, session, CSRF, no caching. The public
 * chain covers everything else and changes nothing: no session, no cookie, no CSRF token, no extra headers (Caddy
 * sets them), so the privacy notice's "no cookies" stays true.
 * <p>
 * Implements {@link WebMvcConfigurer} so that {@code @WebMvcTest} slices pick it up too.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(AdminProperties.class)
public class SecurityConfig implements WebMvcConfigurer {

	/**
	 * Failed logins from one address before it is locked.
	 */
	static final int MAX_FAILURES_PER_IP = 5;

	/**
	 * Failed logins from all addresses before the account is locked; higher, so one attacker cannot easily lock the
	 * admin out.
	 */
	static final int MAX_FAILURES_PER_ACCOUNT = 20;

	@Bean
	@Order(1)
	SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
		// The AdminAuthenticationProvider bean is the global provider. Adding it here as well would run it twice per
		// failed login (once more through the parent manager) and count every failure double.
		http.securityMatcher("/en/admin/**", "/de/admin/**", "/admin/**")
			.authorizeHttpRequests((requests) -> requests.requestMatchers("/en/admin/login", "/de/admin/login")
				.permitAll()
				.anyRequest()
				.hasRole("ADMIN"))
			.formLogin((form) -> form.loginPage("/en/admin/login")
				.loginProcessingUrl("/admin/login")
				.authenticationDetailsSource(AdminLoginDetails::from)
				.successHandler((request, response, authentication) -> redirect(request, response,
						"/" + formLanguage(request) + "/admin"))
				.failureHandler((request, response, exception) -> redirect(request, response, "/" + formLanguage(request)
						+ "/admin/login?" + ((exception instanceof LockedException) ? "locked" : "error"))))
			.logout((logout) -> logout.logoutUrl("/admin/logout")
				.logoutSuccessHandler((request, response, authentication) -> redirect(request, response,
						"/" + formLanguage(request) + "/admin/login?logout")))
			.exceptionHandling((exceptions) -> exceptions
				.authenticationEntryPoint((request, response, exception) -> redirect(request, response,
						"/" + pathLanguage(request) + "/admin/login")))
			.requestCache((cache) -> cache.disable())
			.headers((headers) -> headers.addHeaderWriter(new StaticHeadersWriter("X-Robots-Tag", "noindex, nofollow")));
		return http.build();
	}

	@Bean
	@Order(2)
	SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests((requests) -> requests.anyRequest().permitAll())
			.csrf((csrf) -> csrf.disable())
			.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.requestCache((cache) -> cache.disable())
			.headers((headers) -> headers.disable());
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Uses the application {@link Clock} where there is one (tests replace it), else the system clock.
	 */
	@Bean
	AdminAuthenticationProvider adminAuthenticationProvider(AdminProperties properties,
			PasswordEncoder passwordEncoder, ObjectProvider<Clock> clockProvider) {
		Clock clock = clockProvider.getIfAvailable(Clock::systemUTC);
		return new AdminAuthenticationProvider(properties, passwordEncoder, new Totp(clock),
				new LoginAttemptLimiter(clock, MAX_FAILURES_PER_IP),
				new LoginAttemptLimiter(clock, MAX_FAILURES_PER_ACCOUNT));
	}

	/**
	 * Language of the login or logout form, from its hidden {@code lang} field; only {@code en} or {@code de}, so the
	 * redirect cannot be pointed elsewhere.
	 */
	private static String formLanguage(HttpServletRequest request) {
		String lang = request.getParameter("lang");
		return Languages.SUPPORTED.contains(lang) ? lang : Languages.DEFAULT;
	}

	private static String pathLanguage(HttpServletRequest request) {
		return Languages.fromPath(Languages.requestPath(request)).orElse(Languages.DEFAULT);
	}

	private static void redirect(HttpServletRequest request, HttpServletResponse response, String path)
			throws IOException {
		response.sendRedirect(request.getContextPath() + path);
	}

}
