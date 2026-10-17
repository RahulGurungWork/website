package ch.gurung.rahul.portfolio.home;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

	@Autowired
	private MockMvc mvc;

	@Test
	void rootRedirectsGermanBrowserToGerman() throws Exception {
		this.mvc.perform(get("/").header(HttpHeaders.ACCEPT_LANGUAGE, "de-CH,de;q=0.9,en;q=0.8"))
			.andExpect(status().isFound())
			.andExpect(redirectedUrl("/de"))
			.andExpect(header().string(HttpHeaders.VARY, containsString("Accept-Language")));
	}

	@Test
	void rootRedirectsToEnglishByDefault() throws Exception {
		this.mvc.perform(get("/")).andExpect(status().isFound()).andExpect(redirectedUrl("/en"));
		this.mvc.perform(get("/").header(HttpHeaders.ACCEPT_LANGUAGE, "fr-CH"))
			.andExpect(redirectedUrl("/en"));
	}

	@Test
	void englishHomePage() throws Exception {
		assertThat(html("/en")).contains("<html lang=\"en\"", "Hello!", "Skip to main content",
				"href=\"/en/projects\"", "Legal notice");
	}

	@Test
	void germanHomePage() throws Exception {
		assertThat(html("/de")).contains("<html lang=\"de\"", "Guten Tag!", "Zum Hauptinhalt springen",
				"href=\"/de/projects\"", "Impressum");
	}

	@Test
	void languageSwitcherLinksToSamePageAndMarksCurrentLanguage() throws Exception {
		assertThat(html("/de")).contains(
				"<a href=\"/en\" lang=\"en\" hreflang=\"en\"><span aria-hidden=\"true\">EN</span>",
				"<a href=\"/de\" lang=\"de\" hreflang=\"de\" aria-current=\"true\"><span aria-hidden=\"true\">DE</span>");
	}

	@Test
	void marksHomeAsCurrentPageAndListsAlternates() throws Exception {
		assertThat(html("/en")).contains("<a href=\"/en\" aria-current=\"page\">Home</a>",
				"<link rel=\"alternate\" hreflang=\"de\" href=\"http://localhost/de\">");
	}

	@Test
	void unsupportedLanguageIsNotFound() throws Exception {
		this.mvc.perform(get("/fr")).andExpect(status().isNotFound());
	}

	/**
	 * Body of a successful GET with whitespace runs collapsed, so assertions don't depend on template line breaks.
	 */
	private String html(String path) throws Exception {
		String body = this.mvc.perform(get(path))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		return body.replaceAll("\\s+", " ");
	}

}
