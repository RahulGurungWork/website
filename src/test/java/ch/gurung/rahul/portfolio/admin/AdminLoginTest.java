package ch.gurung.rahul.portfolio.admin;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ADR 0005: the admin area needs username, password and a fresh authenticator code; failures lock the address; the
 * session ends on logout.
 */
@WebMvcTest(AdminController.class)
class AdminLoginTest {

	private static final String USERNAME = "rahul";

	private static final String PASSWORD = "correct horse battery";

	private static final byte[] KEY = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);

	private static final MutableClock CLOCK = new MutableClock(Instant.parse("2026-10-29T12:00:00Z"));

	@Autowired
	private MockMvc mvc;

	@DynamicPropertySource
	static void adminAccount(DynamicPropertyRegistry registry) {
		registry.add("portfolio.admin.username", () -> USERNAME);
		registry.add("portfolio.admin.password-hash", () -> new BCryptPasswordEncoder(4).encode(PASSWORD));
		registry.add("portfolio.admin.totp-secret", () -> Base32.encode(KEY));
	}

	/**
	 * Each test starts an hour later, so lockouts and used codes from other tests have expired.
	 */
	@BeforeEach
	void nextHour() {
		CLOCK.advance(Duration.ofHours(1));
	}

	@Test
	void adminPagesRedirectToLoginInTheirLanguage() throws Exception {
		this.mvc.perform(get("/de/admin")).andExpect(status().isFound()).andExpect(redirectedUrl("/de/admin/login"));
		this.mvc.perform(get("/en/admin/projects"))
			.andExpect(status().isFound())
			.andExpect(redirectedUrl("/en/admin/login"));
	}

	@Test
	void loginPageHasCodeFieldCsrfTokenAndIsNotIndexed() throws Exception {
		MvcResult result = this.mvc.perform(get("/de/admin/login"))
			.andExpect(status().isOk())
			.andExpect(header().string("X-Robots-Tag", "noindex, nofollow"))
			.andReturn();

		assertThat(result.getResponse().getHeader("Cache-Control")).contains("no-store");
		assertThat(result.getResponse().getContentAsString()).contains("<h1>Anmelden</h1>",
				"autocomplete=\"one-time-code\"", "name=\"_csrf\"", "name=\"lang\" value=\"de\"",
				"<meta name=\"robots\" content=\"noindex, nofollow\">")
			.doesNotContain("noch nicht eingerichtet");
	}

	@Test
	void loginWithoutCsrfTokenIsRejected() throws Exception {
		this.mvc.perform(login(currentCode())).andExpect(status().isForbidden());
	}

	@Test
	void correctLoginOpensDashboard() throws Exception {
		MockHttpSession session = loginSucceeds("de");

		String html = this.mvc.perform(get("/de/admin").session(session))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		assertThat(html).contains("<h1>Übersicht</h1>", "Angemeldet als rahul.", "action=\"/admin/logout\"");
	}

	@Test
	void wrongPasswordOrCodeFailsWithoutSayingWhich() throws Exception {
		this.mvc.perform(login(USERNAME, "wrong", currentCode(), "en").with(csrf()))
			.andExpect(redirectedUrl("/en/admin/login?error"));
		this.mvc.perform(login("000000").with(csrf())).andExpect(redirectedUrl("/en/admin/login?error"));
		this.mvc.perform(login("admin", PASSWORD, currentCode(), "en").with(csrf()))
			.andExpect(redirectedUrl("/en/admin/login?error"));
	}

	@Test
	void codeCannotBeUsedTwice() throws Exception {
		String code = currentCode();
		this.mvc.perform(login(code).with(csrf())).andExpect(redirectedUrl("/en/admin"));

		this.mvc.perform(login(code).with(csrf())).andExpect(redirectedUrl("/en/admin/login?error"));
	}

	@Test
	void addressIsLockedAfterFiveFailures() throws Exception {
		for (int i = 0; i < 5; i++) {
			this.mvc.perform(login("000000").with(csrf())).andExpect(redirectedUrl("/en/admin/login?error"));
		}

		this.mvc.perform(login(currentCode()).with(csrf())).andExpect(redirectedUrl("/en/admin/login?locked"));
	}

	@Test
	void logoutEndsSession() throws Exception {
		MockHttpSession session = loginSucceeds("de");

		this.mvc.perform(post("/admin/logout").param("lang", "de").session(session).with(csrf()))
			.andExpect(redirectedUrl("/de/admin/login?logout"));
		this.mvc.perform(get("/de/admin").session(session)).andExpect(redirectedUrl("/de/admin/login"));
	}

	private MockHttpSession loginSucceeds(String lang) throws Exception {
		MvcResult result = this.mvc.perform(login(USERNAME, PASSWORD, currentCode(), lang).with(csrf()))
			.andExpect(redirectedUrl("/" + lang + "/admin"))
			.andReturn();
		return (MockHttpSession) result.getRequest().getSession(false);
	}

	private static MockHttpServletRequestBuilder login(String code) {
		return login(USERNAME, PASSWORD, code, "en");
	}

	private static MockHttpServletRequestBuilder login(String username, String password, String code, String lang) {
		return post("/admin/login").param("username", username)
			.param("password", password)
			.param("code", code)
			.param("lang", lang);
	}

	private static String currentCode() {
		return Totp.codeAt(KEY, CLOCK.instant().getEpochSecond() / Totp.STEP_SECONDS);
	}

	@TestConfiguration(proxyBeanMethods = false)
	static class FixedClock {

		@Bean
		Clock clock() {
			return CLOCK;
		}

	}

}
