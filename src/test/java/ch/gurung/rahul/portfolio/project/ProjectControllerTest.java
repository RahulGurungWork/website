package ch.gurung.rahul.portfolio.project;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private ProjectService projectService;

	@Test
	void listShowsProjectsAsCardsLinkingToDetailPages() throws Exception {
		given(this.projectService.publishedProjects(Locale.of("de"))).willReturn(List.of(new ProjectSummary(
				"n8n-workflow", "n8n-Workflow", "Ein automatisierter Ablauf.", ProjectStatus.CURRENT,
				List.of("n8n", "REST API"), null)));

		assertThat(html("/de/projects")).contains("<html lang=\"de\"", "<h1>Projekte</h1>", "<h2>n8n-Workflow</h2>",
				"<li>REST API</li>", "<svg class=\"art\" viewBox=\"0 0 320 180\" aria-hidden=\"true\"", "url(#art-dots-n8n)", "href=\"/de/projects/n8n-workflow\"",
				"<span class=\"visually-hidden\"> zum Projekt n8n-Workflow</span>",
				"<a href=\"/de/projects\" aria-current=\"page\">Projekte</a>");
	}

	@Test
	void listWithoutProjectsSaysSo() throws Exception {
		given(this.projectService.publishedProjects(any())).willReturn(List.of());

		assertThat(html("/en/projects")).contains("No projects are published yet.").doesNotContain("class=\"cards\"");
	}

	@Test
	void detailShowsProjectInRequestedLanguage() throws Exception {
		given(this.projectService.publishedProject("n8n-workflow", Locale.of("en"))).willReturn(Optional.of(
				new ProjectDetail("n8n-workflow", "n8n workflow", "Moves data.", "First paragraph.\n\nSecond paragraph.",
						ProjectStatus.PAST, LocalDate.of(2025, 3, 1), LocalDate.of(2025, 9, 30),
						"https://github.com/RahulGurungWork/n8n", null, List.of("n8n"),
						List.of(new ProjectDetail.Image("n8n-workflow/flow.webp", "The workflow canvas")))));

		assertThat(html("/en/projects/n8n-workflow")).contains("<title>n8n workflow – Rahul Gurung</title>",
				"<h1>n8n workflow</h1>", "Past project", "<time datetime=\"2025\">2025</time>",
				"<p>First paragraph.</p>", "<p>Second paragraph.</p>",
				"href=\"https://github.com/RahulGurungWork/n8n\"",
				"<img src=\"/img/projects/n8n-workflow/flow.webp\" alt=\"The workflow canvas\"",
				"<a href=\"/en/projects\" aria-current=\"true\">Projects</a>",
				"<a href=\"/de/projects/n8n-workflow\" lang=\"de\"")
			.doesNotContain("Live demo", "<svg class=\"art\"");
	}

	@Test
	void detailWithoutScreenshotsShowsIllustrationInsteadOfGallery() throws Exception {
		given(this.projectService.publishedProject("lerndoku-generator", Locale.of("de"))).willReturn(Optional.of(
				new ProjectDetail("lerndoku-generator", "Lerndokumentations-Generator", "Ein Werkzeug.", "Text.",
						ProjectStatus.CURRENT, null, null, null, null, List.of("Java"), List.of())));

		assertThat(html("/de/projects/lerndoku-generator")).contains("<div class=\"project-visual\">",
				"<svg class=\"art\" viewBox=\"0 0 320 180\" aria-hidden=\"true\" focusable=\"false\"",
				"class=\"art-term\"")
			.doesNotContain("class=\"gallery\"", "<img");
	}

	@Test
	void unknownSlugGetsGenericIllustration() throws Exception {
		given(this.projectService.publishedProject("new-project", Locale.of("en"))).willReturn(Optional.of(
				new ProjectDetail("new-project", "New project", "Summary.", "Text.", ProjectStatus.CURRENT, null, null,
						null, null, List.of(), List.of())));

		assertThat(html("/en/projects/new-project")).contains("<svg class=\"art\"", "class=\"art-tint\"")
			.doesNotContain("art-dots-n8n", "art-term");
	}

	@Test
	void unknownProjectIsNotFound() throws Exception {
		given(this.projectService.publishedProject(anyString(), any())).willReturn(Optional.empty());

		this.mvc.perform(get("/en/projects/does-not-exist")).andExpect(status().isNotFound());
	}

	@Test
	void unsupportedLanguageIsNotFound() throws Exception {
		this.mvc.perform(get("/fr/projects")).andExpect(status().isNotFound());
	}

	private String html(String path) throws Exception {
		String body = this.mvc.perform(get(path))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		return body.replaceAll("\\s+", " ");
	}

}
