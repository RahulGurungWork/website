package ch.gurung.rahul.portfolio.home;

import java.util.List;
import java.util.Locale;

import ch.gurung.rahul.portfolio.project.ProjectService;
import ch.gurung.rahul.portfolio.project.ProjectStatus;
import ch.gurung.rahul.portfolio.project.ProjectSummary;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private ProjectService projectService;

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
	void showsProjectCardsAndCountInGerman() throws Exception {
		given(this.projectService.publishedProjects(Locale.of("de"))).willReturn(List.of(
				new ProjectSummary("n8n-workflow", "n8n-Workflow", "Ein Ablauf.", ProjectStatus.CURRENT, List.of("n8n"),
						null),
				new ProjectSummary("lerndoku-generator", "Lerndokumentations-Generator", "Ein Werkzeug.",
						ProjectStatus.CURRENT, List.of("Java"), null)));

		assertThat(html("/de")).contains("· 2 Projekte", "<h2 id=\"projects-title\">Ausgewählte Projekte</h2>",
				"<h3>n8n-Workflow</h3>", "<h3>Lerndokumentations-Generator</h3>",
				"href=\"/de/projects/lerndoku-generator\"", "Alle Projekte");
	}

	@Test
	void showsApprenticeshipSkillsAndAbout() throws Exception {
		assertThat(html("/en")).contains("training as an Informatiker EFZ",
				"<h2 id=\"skills-title\">What I work with</h2>", "<h3>Frameworks and databases</h3>",
				"<span>Spring Boot</span> <span class=\"level level-learning\">Learning</span>",
				"<span>Notion</span> <span class=\"level level-strong\">Strong</span>",
				"<h2 id=\"about-title\">About me</h2>", "philosophy club", "Nepali, basic",
				"<a class=\"btn btn-outline-ink\" href=\"/en/cv.pdf\">Download CV (PDF)</a>");
		assertThat(html("/de")).contains("Lehre als Informatiker EFZ Applikationsentwicklung",
				"<span>Java</span> <span class=\"level level-comfortable\">Sicher</span>", "<h2 id=\"about-title\">Über mich</h2>",
				"Tischtennis");
	}

	@Test
	void hidesProjectSectionWithoutProjects() throws Exception {
		assertThat(html("/en")).contains("· 0 projects").doesNotContain("Selected projects");
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
