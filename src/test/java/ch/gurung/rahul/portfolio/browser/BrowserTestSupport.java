package ch.gurung.rahul.portfolio.browser;

import java.util.List;
import java.util.stream.Collectors;

import ch.gurung.rahul.portfolio.TestcontainersConfiguration;
import com.deque.html.axecore.playwright.AxeBuilder;
import com.deque.html.axecore.results.AxeResults;
import com.deque.html.axecore.results.Rule;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the real application (PostgreSQL via Testcontainers) and a headless Chromium. Playwright downloads the
 * browser on first use; on Linux it also needs system libraries ({@code playwright install --with-deps chromium},
 * see the CI workflow).
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
abstract class BrowserTestSupport {

	private static final List<String> WCAG_21_AA = List.of("wcag2a", "wcag2aa", "wcag21a", "wcag21aa");

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

	/**
	 * axe-core finds no WCAG 2.1 A/AA violations on the page as it is now.
	 */
	protected static void assertNoViolations(Page page, String description) {
		AxeResults results = new AxeBuilder(page).withTags(WCAG_21_AA).analyze();

		assertThat(results.getViolations()).as(() -> description + ":\n" + describe(results.getViolations()))
			.isEmpty();
	}

	private static String describe(List<Rule> violations) {
		return violations.stream()
			.map((rule) -> "- " + rule.getId() + ": " + rule.getHelp() + " " + rule.getNodes()
				.stream()
				.map((node) -> String.valueOf(node.getTarget()))
				.collect(Collectors.joining(", ")))
			.collect(Collectors.joining("\n"));
	}

}
