package ch.gurung.rahul.portfolio.browser;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Pattern;

import ch.gurung.rahul.portfolio.admin.Base32;
import ch.gurung.rahul.portfolio.admin.MutableClock;
import ch.gurung.rahul.portfolio.admin.Totp;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.ColorScheme;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Admin panel in a real browser (step A5): login with password and authenticator code, a project created in the
 * admin appears on the public site, logout, and no WCAG 2.1 A/AA violations on any admin page.
 */
class AdminBrowserIT extends BrowserTestSupport {

	private static final String USERNAME = "rahul";

	private static final String PASSWORD = "browser test password";

	private static final byte[] KEY = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);

	private static final MutableClock CLOCK = new MutableClock(Instant.parse("2026-10-30T12:00:00Z"));

	@DynamicPropertySource
	static void adminAccount(DynamicPropertyRegistry registry) {
		registry.add("portfolio.admin.username", () -> USERNAME);
		registry.add("portfolio.admin.password-hash", () -> new BCryptPasswordEncoder(4).encode(PASSWORD));
		registry.add("portfolio.admin.totp-secret", () -> Base32.encode(KEY));
	}

	@Test
	void projectCreatedInAdminAppearsOnTheSite() {
		Page page = loggedIn("en", ColorScheme.LIGHT);

		page.getByRole(AriaRole.NAVIGATION)
			.getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Projects").setExact(true))
			.click();
		page.getByRole(AriaRole.LINK, link("New project")).click();
		page.locator("#slug").fill("browser-test-project");
		page.locator("#newTechnologies").fill("Playwright");
		page.getByLabel("Show on the website").check();
		page.locator("#titleEn").fill("Browser test project");
		page.locator("#summaryEn").fill("Created by AdminBrowserIT.");
		page.locator("#descriptionEn").fill("First paragraph.\n\nSecond paragraph.");
		page.locator("#titleDe").fill("Browser-Testprojekt");
		page.locator("#summaryDe").fill("Erstellt von AdminBrowserIT.");
		page.locator("#descriptionDe").fill("Erster Absatz.");
		page.getByRole(AriaRole.BUTTON, button("Save")).click();

		assertThat(page).hasURL(Pattern.compile(".*/en/admin/projects$"));
		assertThat(page.getByRole(AriaRole.STATUS)).hasText("Project created.");

		page.navigate("/de/projects/browser-test-project");
		assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setLevel(1)))
			.hasText("Browser-Testprojekt");
		assertThat(page.getByText("Playwright")).isVisible();
	}

	@Test
	void wrongCodeIsRejectedAndLogoutEndsTheSession() {
		Page page = newPage();
		page.navigate("/de/admin");
		assertThat(page).hasURL(Pattern.compile(".*/de/admin/login$"));

		submitLogin(page, "000000");
		assertThat(page.getByRole(AriaRole.ALERT)).containsText("Anmeldung fehlgeschlagen.");

		submitLogin(page, nextCode());
		assertThat(page).hasURL(Pattern.compile(".*/de/admin$"));
		page.getByRole(AriaRole.BUTTON, button("Abmelden")).click();

		assertThat(page).hasURL(Pattern.compile(".*/de/admin/login\\?logout$"));
		assertThat(page.getByRole(AriaRole.STATUS)).hasText("Sie sind abgemeldet.");
		page.navigate("/de/admin/projects");
		assertThat(page).hasURL(Pattern.compile(".*/de/admin/login$"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "/en/admin", "/de/admin/projects", "/en/admin/projects/new", "/de/admin/certificates",
			"/en/admin/certificates/new", "/de/admin/messages" })
	void adminPagesHaveNoViolations(String path) {
		Page page = loggedIn(path.substring(1, 3), ColorScheme.LIGHT);
		page.navigate(path);

		assertNoViolations(page, path);
	}

	@Test
	void adminPagesHaveNoViolationsInDarkMode() {
		Page page = loggedIn("en", ColorScheme.DARK);
		page.navigate("/en/admin/projects");

		assertNoViolations(page, "/en/admin/projects (dark)");
	}

	@Test
	void loginPageHasNoViolationsAlsoWithError() {
		Page page = newPage();
		page.navigate("/en/admin/login");
		assertNoViolations(page, "/en/admin/login");

		submitLogin(page, "000000");
		assertNoViolations(page, "/en/admin/login?error");
	}

	@Test
	void formWithErrorsAndConfirmPagesHaveNoViolations() {
		Page page = loggedIn("en", ColorScheme.LIGHT);

		page.navigate("/en/admin/projects/new");
		page.getByRole(AriaRole.BUTTON, button("Save")).click();
		assertThat(page.getByRole(AriaRole.ALERT)).isVisible();
		assertNoViolations(page, "/en/admin/projects/new with errors");

		page.navigate("/en/admin/projects");
		page.locator("a.admin-danger").first().click();
		assertNoViolations(page, "project delete confirmation");
	}

	@Test
	void messagePageHasNoViolations() {
		Page visitor = newPage();
		visitor.navigate("/en/contact");
		visitor.getByLabel("Name").fill("Inbox Test");
		visitor.getByLabel("Email").fill("inbox.test@example.ch");
		visitor.getByLabel("Message").fill("Line one.\nLine two.");
		visitor.getByRole(AriaRole.BUTTON, button("Send")).click();
		assertThat(visitor).hasURL(Pattern.compile(".*/en/contact/sent$"));

		Page page = loggedIn("en", ColorScheme.LIGHT);
		page.navigate("/en/admin/messages");
		page.getByRole(AriaRole.LINK, link("Inbox Test")).first().click();

		assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setLevel(1)))
			.hasText("Message from Inbox Test");
		assertNoViolations(page, "message detail");
	}

	private Page loggedIn(String lang, ColorScheme colorScheme) {
		Page page = newPage("en-GB", colorScheme);
		page.navigate("/" + lang + "/admin");
		submitLogin(page, nextCode());
		assertThat(page).hasURL(Pattern.compile(".*/" + lang + "/admin$"));
		return page;
	}

	private static void submitLogin(Page page, String code) {
		page.locator("#username").fill(USERNAME);
		page.locator("#password").fill(PASSWORD);
		page.locator("#code").fill(code);
		page.locator("main form button[type=submit]").click();
	}

	/**
	 * Moves the app's clock on, so every login uses a fresh code (each code is accepted only once).
	 */
	private static String nextCode() {
		CLOCK.advance(Duration.ofMinutes(1));
		return Totp.codeAt(KEY, CLOCK.instant().getEpochSecond() / 30);
	}

	private static Page.GetByRoleOptions link(String name) {
		return new Page.GetByRoleOptions().setName(name).setExact(true);
	}

	private static Page.GetByRoleOptions button(String name) {
		return new Page.GetByRoleOptions().setName(name).setExact(true);
	}

	/**
	 * The app's clock for this test context; the admin login reads it for authenticator codes.
	 */
	@TestConfiguration(proxyBeanMethods = false)
	static class TestClock {

		@Bean
		@Primary
		Clock testClock() {
			return CLOCK;
		}

	}

}
