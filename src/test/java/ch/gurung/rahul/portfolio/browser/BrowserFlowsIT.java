package ch.gurung.rahul.portfolio.browser;

import java.util.regex.Pattern;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.ColorScheme;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * Key flows in a real browser (e2e/README.md, test concept FR1–FR3, FR6, FR11–FR13).
 */
class BrowserFlowsIT extends BrowserTestSupport {

	@Test
	void rootSendsGermanBrowserToGerman() {
		Page page = newPage("de-CH", ColorScheme.LIGHT);

		page.navigate("/");

		assertThat(page).hasURL(Pattern.compile(".*/de$"));
		assertThat(page.locator("html")).hasAttribute("lang", "de");
	}

	@ParameterizedTest
	@CsvSource(delimiter = '|', value = { "/en | More about n8n workflow | /en/projects/n8n-workflow | n8n workflow",
			"/de | Mehr zum Projekt n8n-Workflow | /de/projects/n8n-workflow | n8n-Workflow" })
	void homeLeadsToProjectDetail(String start, String linkName, String detailPath, String heading) {
		Page page = newPage();
		page.navigate(start);

		page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName(linkName).setExact(true)).click();

		assertThat(page).hasURL(Pattern.compile(".*" + detailPath + "$"));
		assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setLevel(1))).hasText(heading);
	}

	@Test
	void languageSwitcherKeepsThePage() {
		Page page = newPage();
		page.navigate("/en/projects/n8n-workflow");

		page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Deutsch").setExact(true)).click();

		assertThat(page).hasURL(Pattern.compile(".*/de/projects/n8n-workflow$"));
		assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setLevel(1))).hasText("n8n-Workflow");
	}

	@Test
	void contactFormShowsErrorsThenConfirms() {
		Page page = newPage();
		page.navigate("/en/contact");

		page.getByLabel("Name").fill("Browser Test");
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Send")).click();

		assertThat(page.getByRole(AriaRole.ALERT)).containsText("Enter your email address.");
		assertThat(page.getByLabel("Email")).hasAttribute("aria-invalid", "true");
		assertThat(page.getByLabel("Name")).hasValue("Browser Test");

		page.getByLabel("Email").fill("browser.test@example.ch");
		page.getByLabel("Message").fill("Sent by BrowserFlowsIT.");
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Send")).click();

		assertThat(page).hasURL(Pattern.compile(".*/en/contact/sent$"));
		assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setLevel(1))).hasText("Thank you!");
	}

	@ParameterizedTest
	@ValueSource(strings = { "/en", "/en/projects", "/en/certificates", "/en/contact", "/en/does-not-exist" })
	void legalPagesAreLinkedFromEveryFooter(String start) {
		Page page = newPage();
		page.navigate(start);

		page.getByRole(AriaRole.CONTENTINFO).getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Privacy")).click();
		assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setLevel(1))).hasText("Privacy notice");

		page.getByRole(AriaRole.CONTENTINFO).getByRole(AriaRole.LINK, new Locator.GetByRoleOptions().setName("Legal notice")).click();
		assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setLevel(1))).hasText("Legal notice");
	}

	@Test
	void skipLinkIsFirstAndVisibleOnFocus() {
		Page page = newPage();
		page.navigate("/en");

		page.keyboard().press("Tab");

		assertThat(page.locator(":focus")).hasText("Skip to main content");
		assertThat(page.locator(":focus")).isInViewport();
		page.keyboard().press("Enter");
		assertThat(page).hasURL(Pattern.compile(".*#main$"));
	}

}
