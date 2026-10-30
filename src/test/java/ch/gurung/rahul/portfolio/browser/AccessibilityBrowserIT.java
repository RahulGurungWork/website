package ch.gurung.rahul.portfolio.browser;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.ColorScheme;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * axe-core finds no WCAG 2.1 A/AA violations on any page type, in both languages and colour schemes (NVDA, keyboard
 * and zoom stay on the manual checklist in the test concept). Admin pages: {@link AdminBrowserIT}.
 */
class AccessibilityBrowserIT extends BrowserTestSupport {

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

}
