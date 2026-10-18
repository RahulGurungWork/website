package ch.gurung.rahul.portfolio.browser;

import ch.gurung.rahul.portfolio.TestcontainersConfiguration;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.ColorScheme;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;

/**
 * Runs the real application (PostgreSQL via Testcontainers) and a headless Chromium. Playwright downloads the
 * browser on first use; on Linux it also needs system libraries ({@code playwright install --with-deps chromium},
 * see the CI workflow).
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
abstract class BrowserTestSupport {

	private static Playwright playwright;

	private static Browser browser;

	@Value("${local.server.port}")
	private int port;

	private BrowserContext context;

	@BeforeAll
	static void launchBrowser() {
		playwright = Playwright.create();
		browser = playwright.chromium().launch();
	}

	@AfterAll
	static void closeBrowser() {
		if (playwright != null) {
			playwright.close();
		}
	}

	@AfterEach
	void closeContext() {
		if (this.context != null) {
			this.context.close();
		}
	}

	/**
	 * New page whose browser sends {@code acceptLanguage}; relative URLs resolve against the running app.
	 */
	protected Page newPage(String acceptLanguage, ColorScheme colorScheme) {
		this.context = browser.newContext(new Browser.NewContextOptions().setBaseURL("http://localhost:" + this.port)
			.setLocale(acceptLanguage)
			.setColorScheme(colorScheme));
		return this.context.newPage();
	}

	protected Page newPage() {
		return newPage("en-GB", ColorScheme.LIGHT);
	}

}
