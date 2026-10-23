package ch.gurung.rahul.portfolio.certification;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Acceptance criterion FR4: certifications show issuer, date and credential link, grouped into sections.
 */
@WebMvcTest(CertificationController.class)
class CertificationControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private CertificationService certificationService;

	@Test
	void listsGroupsWithIssuerDateAndCredentialLink() throws Exception {
		given(this.certificationService.groupedCertifications()).willReturn(List.of(
				new CertificationEntry.Group(CertificationCategory.LANGUAGE_AND_SCHOOL,
						List.of(new CertificationEntry("Cambridge English: C1 Advanced",
								"Cambridge University Press & Assessment", null, null,
								CertificationCategory.LANGUAGE_AND_SCHOOL))),
				new CertificationEntry.Group(CertificationCategory.TECHNICAL,
						List.of(new CertificationEntry("Oracle Certified Professional: Java SE 21 Developer", "Oracle",
								LocalDate.of(2026, 6, 15), "https://example.com/credential/123",
								CertificationCategory.TECHNICAL)))));

		assertThat(html("/de/certificates")).contains("<h1>Zertifikate</h1>",
				"<h2 id=\"cert-language_and_school\">Sprachen und Schule</h2>",
				"<section class=\"cert-group\" aria-labelledby=\"cert-technical\">",
				"<h2 id=\"cert-technical\">Technik</h2>",
				"<h3>Oracle Certified Professional: Java SE 21 Developer</h3>", "<span class=\"issuer\">Oracle</span>",
				"<time datetime=\"2026-06\">06.2026</time>", "href=\"https://example.com/credential/123\"",
				"<span>Nachweis</span><span class=\"visually-hidden\"> für Oracle Certified Professional: Java SE 21 Developer</span>",
				"<span class=\"issuer\">Cambridge University Press &amp; Assessment</span>",
				"<a href=\"/de/certificates\" aria-current=\"page\">Zertifikate</a>")
			.doesNotContain("Berufliche Kompetenzen");
	}

	@Test
	void saysSoWhenThereAreNoCertifications() throws Exception {
		assertThat(html("/en/certificates")).contains("Certificates will be listed here soon.")
			.doesNotContain("class=\"cert-group\"");
	}

	@Test
	void unsupportedLanguageIsNotFound() throws Exception {
		this.mvc.perform(get("/fr/certificates")).andExpect(status().isNotFound());
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
