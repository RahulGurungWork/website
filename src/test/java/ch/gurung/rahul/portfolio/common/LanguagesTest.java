package ch.gurung.rahul.portfolio.common;

import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class LanguagesTest {

	@ParameterizedTest
	@CsvSource({ "/de, de", "/de/projects, de", "/en/projects/n8n-workflow, en" })
	void readsLanguageFromFirstSegment(String path, String language) {
		assertThat(Languages.fromPath(path)).hasValue(language);
	}

	@ParameterizedTest
	@CsvSource({ "/", "/fr/projects", "/deutsch", "/css/site.css", "/error" })
	void ignoresPathsWithoutSupportedLanguage(String path) {
		assertThat(Languages.fromPath(path)).isEmpty();
	}

	@ParameterizedTest
	@CsvSource({ "/de, ''", "/de/projects, /projects", "/en/projects/n8n-workflow, /projects/n8n-workflow",
			"/css/site.css, /css/site.css" })
	void stripsLanguageSegment(String path, String expected) {
		assertThat(Languages.withoutLanguage(path)).isEqualTo(expected);
	}

	@ParameterizedTest
	@CsvSource(delimiter = '|', value = { "de-CH,de;q=0.9,en;q=0.8 | de", "en-US,de;q=0.5 | en", "fr-CH,de;q=0.7 | de",
			"fr-CH,fr;q=0.9 | en", "not a header;;q=x | en" })
	void picksBestLanguageFromAcceptLanguage(String header, String expected) {
		assertThat(Languages.fromAcceptLanguage(header)).isEqualTo(expected);
	}

	@ParameterizedTest
	@NullAndEmptySource
	void defaultsToEnglishWithoutAcceptLanguage(String header) {
		assertThat(Languages.fromAcceptLanguage(header)).isEqualTo("en");
	}

	@Test
	void usesOriginalPathOnErrorDispatch() {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/error");
		request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, "/de/missing");

		assertThat(Languages.requestPath(request)).isEqualTo("/de/missing");
	}

}
