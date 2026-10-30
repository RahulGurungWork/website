package ch.gurung.rahul.portfolio.certification;

import java.time.LocalDate;
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
 * Step A3: certificate admin pages need a login, validate input in the admin's language, and change data only
 * through POST with a CSRF token.
 */
@WebMvcTest(CertificationAdminController.class)
class CertificationAdminControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private CertificationAdminService service;

	@Test
	void loggedOutVisitorsAreSentToLogin() throws Exception {
		this.mvc.perform(get("/en/admin/certificates")).andExpect(redirectedUrl("/en/admin/login"));
		this.mvc.perform(post("/de/admin/certificates/1/delete").with(csrf()))
			.andExpect(redirectedUrl("/de/admin/login"));
		then(this.service).should(never()).delete(anyLong());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void listShowsGroupsWithHeadings() throws Exception {
		given(this.service.list()).willReturn(List.of(new CertificationAdminService.Group(
				CertificationCategory.TECHNICAL,
				List.of(new CertificationAdminService.Row(7, "Learn Bootstrap", "Codecademy", LocalDate.of(2024, 7, 26),
						true, true)))));

		String html = html(get("/de/admin/certificates"));

		assertThat(html).contains("<h1>Zertifikate</h1>", ">Technik</h2>", "Learn Bootstrap", "Codecademy",
				"<time datetime=\"2024-07-26\">26.07.2024</time>", "href=\"/de/admin/certificates/7/edit\"",
				"aria-current=\"page\">Zertifikate</a>")
			.doesNotContain("value=\"up\"", "value=\"down\"");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void invalidInputShowsErrorsInLanguage() throws Exception {
		String html = html(valid(post("/en/admin/certificates/new"), "name", "", "credentialUrl", "example.com")
			.with(csrf()));

		assertThat(html).contains("role=\"alert\"", "Name: Please fill in this field.",
				"Credential link: Enter a full link starting with https://", "value=\"example.com\"");
		then(this.service).should(never()).create(any());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void validCertificateIsCreated() throws Exception {
		this.mvc.perform(valid(post("/de/admin/certificates/new")).with(csrf()))
			.andExpect(redirectedUrl("/de/admin/certificates"))
			.andExpect(flash().attribute("notice", "admin.certificate.created"));

		then(this.service).should().create(any(CertificationForm.class));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void changesNeedCsrfToken() throws Exception {
		this.mvc.perform(valid(post("/en/admin/certificates/new"))).andExpect(status().isForbidden());
		this.mvc.perform(post("/en/admin/certificates/1/delete")).andExpect(status().isForbidden());
		then(this.service).should(never()).create(any());
		then(this.service).should(never()).delete(anyLong());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void unknownCertificateIsNotFound() throws Exception {
		given(this.service.form(99)).willReturn(Optional.empty());
		given(this.service.update(eq(99L), any())).willReturn(false);

		this.mvc.perform(get("/en/admin/certificates/99/edit")).andExpect(status().isNotFound());
		this.mvc.perform(valid(post("/en/admin/certificates/99/edit")).with(csrf()))
			.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void deleteAsksFirstThenDeletes() throws Exception {
		given(this.service.name(5L)).willReturn(Optional.of("Learn Bash Scripting"));

		assertThat(html(get("/de/admin/certificates/5/delete"))).contains("<h1>Zertifikat löschen?</h1>",
				"«Learn Bash Scripting» verschwindet von der Zertifikate-Seite und aus dem Lebenslauf.");
		then(this.service).should(never()).delete(anyLong());

		this.mvc.perform(post("/de/admin/certificates/5/delete").with(csrf()))
			.andExpect(redirectedUrl("/de/admin/certificates"));
		then(this.service).should().delete(5L);
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void moveRedirectsToList() throws Exception {
		this.mvc.perform(post("/en/admin/certificates/5/move").param("direction", "down").with(csrf()))
			.andExpect(redirectedUrl("/en/admin/certificates"));

		then(this.service).should().move(5L, false);
	}

	private String html(MockHttpServletRequestBuilder request) throws Exception {
		return this.mvc.perform(request).andReturn().getResponse().getContentAsString();
	}

	/**
	 * A valid certificate form; {@code overrides} are name/value pairs that replace or add fields.
	 */
	private static MockHttpServletRequestBuilder valid(MockHttpServletRequestBuilder request, String... overrides) {
		Map<String, String> fields = new LinkedHashMap<>();
		fields.put("name", "Learn Java");
		fields.put("issuer", "Codecademy");
		fields.put("issuedOn", "2026-10-01");
		fields.put("category", "TECHNICAL");
		for (int i = 0; i < overrides.length; i += 2) {
			fields.put(overrides[i], overrides[i + 1]);
		}
		fields.forEach(request::param);
		return request;
	}

}
