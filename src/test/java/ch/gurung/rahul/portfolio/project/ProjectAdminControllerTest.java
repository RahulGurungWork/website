package ch.gurung.rahul.portfolio.project;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Step A2: project admin pages need a login, validate input in the admin's language, and change data only through
 * POST with a CSRF token.
 */
@WebMvcTest(ProjectAdminController.class)
class ProjectAdminControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private ProjectAdminService service;

	@Test
	void loggedOutVisitorsAreSentToLogin() throws Exception {
		this.mvc.perform(get("/de/admin/projects")).andExpect(redirectedUrl("/de/admin/login"));
		this.mvc.perform(post("/en/admin/projects/1/delete").with(csrf())).andExpect(redirectedUrl("/en/admin/login"));
		then(this.service).should(never()).delete(anyLong());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void listShowsProjectsWithVisibilityInWords() throws Exception {
		given(this.service.list(any())).willReturn(List.of(
				new ProjectAdminService.Row(1, "n8n-workflow", "n8n-Workflow", ProjectStatus.CURRENT, true, true, false),
				new ProjectAdminService.Row(2, "draft", "Entwurf", ProjectStatus.PAST, false, false, true)));

		String html = html(get("/de/admin/projects"));

		assertThat(html).contains("<h1>Projekte</h1>", "n8n-Workflow", "Entwurf", "Veröffentlicht", "Versteckt",
				"Abgeschlossenes Projekt", "href=\"/de/admin/projects/2/edit\"", "name=\"_csrf\"",
				"aria-current=\"page\">Projekte</a>");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void newFormHasBothLanguagesAndLabelledFields() throws Exception {
		String html = html(get("/en/admin/projects/new"));

		assertThat(html).contains("<h1>New project</h1>", "action=\"/en/admin/projects/new\"",
				"<label for=\"titleEn\">Title</label>", "id=\"titleDe\"", "lang=\"de\"", "type=\"date\"",
				"name=\"published\"");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void invalidInputShowsErrorsAndKeepsValues() throws Exception {
		String html = html(valid(post("/de/admin/projects/new"), "slug", "Not A Slug", "titleDe", "", "startedOn",
				"2026-10-10", "endedOn", "2026-10-01")
			.with(csrf()));

		assertThat(html).contains("role=\"alert\"", "Nur Kleinbuchstaben, Ziffern und einzelne Bindestriche",
				"Titel (Deutsch): Bitte füllen Sie dieses Feld aus.",
				"Das Abschlussdatum kann nicht vor dem Startdatum liegen.", "value=\"Not A Slug\"",
				"aria-invalid=\"true\"");
		then(this.service).should(never()).create(any());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void takenSlugIsRejected() throws Exception {
		given(this.service.slugTaken(eq("n8n-workflow"), isNull())).willReturn(true);

		String html = html(valid(post("/en/admin/projects/new"), "slug", "n8n-workflow").with(csrf()));

		assertThat(html).contains("Another project already uses this address.");
		then(this.service).should(never()).create(any());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void validNewProjectIsCreatedAndListShown() throws Exception {
		this.mvc.perform(valid(post("/de/admin/projects/new")).with(csrf()))
			.andExpect(redirectedUrl("/de/admin/projects"))
			.andExpect(flash().attribute("notice", "admin.project.created"));

		then(this.service).should().create(any(ProjectForm.class));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void changesNeedCsrfToken() throws Exception {
		this.mvc.perform(valid(post("/en/admin/projects/new"))).andExpect(status().isForbidden());
		this.mvc.perform(post("/en/admin/projects/1/delete")).andExpect(status().isForbidden());
		then(this.service).should(never()).create(any());
		then(this.service).should(never()).delete(anyLong());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void unknownProjectIsNotFound() throws Exception {
		given(this.service.form(99)).willReturn(Optional.empty());
		given(this.service.update(eq(99L), any())).willReturn(false);

		this.mvc.perform(get("/en/admin/projects/99/edit")).andExpect(status().isNotFound());
		this.mvc.perform(valid(post("/en/admin/projects/99/edit")).with(csrf())).andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void deleteAsksFirstThenDeletes() throws Exception {
		given(this.service.title(eq(3L), any())).willReturn(Optional.of("Lerndokumentations-Generator"));

		assertThat(html(get("/de/admin/projects/3/delete"))).contains("<h1>Projekt löschen?</h1>",
				"«Lerndokumentations-Generator» wird mit Texten und Bildern gelöscht.",
				"action=\"/de/admin/projects/3/delete\"");
		then(this.service).should(never()).delete(anyLong());

		this.mvc.perform(post("/de/admin/projects/3/delete").with(csrf()))
			.andExpect(redirectedUrl("/de/admin/projects"));
		then(this.service).should().delete(3L);
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void publishAndMoveRedirectToList() throws Exception {
		given(this.service.setPublished(2L, true)).willReturn(true);

		this.mvc.perform(post("/en/admin/projects/2/publish").param("published", "true").with(csrf()))
			.andExpect(redirectedUrl("/en/admin/projects"))
			.andExpect(flash().attribute("notice", "admin.project.publishedNotice"));
		this.mvc.perform(post("/en/admin/projects/2/move").param("direction", "up").with(csrf()))
			.andExpect(redirectedUrl("/en/admin/projects"));

		then(this.service).should().move(2L, true);
	}

	private String html(MockHttpServletRequestBuilder request) throws Exception {
		return this.mvc.perform(request).andReturn().getResponse().getContentAsString();
	}

	/**
	 * A valid project form; {@code overrides} are name/value pairs that replace or add fields.
	 */
	private static MockHttpServletRequestBuilder valid(MockHttpServletRequestBuilder request, String... overrides) {
		Map<String, String> fields = new LinkedHashMap<>();
		fields.put("slug", "portfolio-website");
		fields.put("status", "CURRENT");
		fields.put("titleEn", "Portfolio website");
		fields.put("summaryEn", "This website.");
		fields.put("descriptionEn", "About it.");
		fields.put("titleDe", "Portfolio-Website");
		fields.put("summaryDe", "Diese Website.");
		fields.put("descriptionDe", "Darüber.");
		for (int i = 0; i < overrides.length; i += 2) {
			fields.put(overrides[i], overrides[i + 1]);
		}
		fields.forEach(request::param);
		return request;
	}

}
