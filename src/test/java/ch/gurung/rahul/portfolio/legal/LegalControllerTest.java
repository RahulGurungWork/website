package ch.gurung.rahul.portfolio.legal;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Acceptance criteria FR12/FR13: legal notice and privacy notice in both languages, linked from the footer.
 */
@WebMvcTest(LegalController.class)
class LegalControllerTest {

	@Autowired
	private MockMvc mvc;

	@ParameterizedTest
	@CsvSource(delimiter = '|', value = { "/en/legal-notice | en | <h1>Legal notice</h1>",
			"/de/legal-notice | de | <h1>Impressum</h1>", "/en/privacy | en | <h1>Privacy notice</h1>",
			"/de/privacy | de | <h1>Datenschutzerklärung</h1>" })
	void rendersPageInItsLanguageWithFooterLinks(String path, String lang, String heading) throws Exception {
		String html = this.mvc.perform(get(path))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString()
			.replaceAll("\\s+", " ");

		assertThat(html).contains("<html lang=\"" + lang + "\"", heading, "rahul@gurung.ch",
				"href=\"/" + lang + "/legal-notice\"", "href=\"/" + lang + "/privacy\"");
	}

	@ParameterizedTest
	@CsvSource({ "/fr/legal-notice", "/fr/privacy" })
	void unsupportedLanguageIsNotFound(String path) throws Exception {
		this.mvc.perform(get(path)).andExpect(status().isNotFound());
	}

}
