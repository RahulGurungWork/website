package ch.gurung.rahul.portfolio.browser;

import java.util.List;
import java.util.stream.Collectors;

import com.deque.html.axecore.playwright.AxeBuilder;
import com.deque.html.axecore.results.AxeResults;
import com.deque.html.axecore.results.Rule;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.ColorScheme;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * axe-core finds no WCAG 2.1 A/AA violations on any page type, in both languages and colour schemes (NVDA, keyboard
 * and zoom stay on the manual checklist in the test concept).
 */
class AccessibilityBrowserIT extends BrowserTestSupport {

	private static final List<String> WCAG_21_AA = List.of("wcag2a", "wcag2aa", "wcag21a", "wcag21aa");

	@ParameterizedTest
	@ValueSource(strings = { "/en", "/de", "/en/projects", "/de/projects", "/en/projects/n8n-workflow",
			"/de/projects/lerndoku-generator", "/en/certificates", "/de/certificates", "/en/contact", "/de/contact",
			"/en/contact/sent", "/en/legal-notice", "/de/legal-notice", "/en/privacy", "/de/privacy",
			"/en/does-not-exist", "/de/does-not-exist" })
	void noViolationsInLightMode(String path) {
		Page page = newPage("en-GB", ColorScheme.LIGHT);
		page.navigate(path);

		assertNoViolations(page, path);
	}

	@ParameterizedTest
	@ValueSource(strings = { "/en", "/de/projects", "/en/projects/n8n-workflow", "/de/certificates", "/en/contact",
			"/de/privacy", "/en/does-not-exist" })
	void noViolationsInDarkMode(String path) {
		Page page = newPage("en-GB", ColorScheme.DARK);
		page.navigate(path);

		assertNoViolations(page, path + " (dark)");
	}

	@Test
	void noViolationsInContactFormWithErrors() {
		Page page = newPage();
		page.navigate("/de/contact");
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Absenden")).click();

		assertNoViolations(page, "/de/contact with errors");
	}

	private static void assertNoViolations(Page page, String description) {
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
