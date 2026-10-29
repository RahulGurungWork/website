package ch.gurung.rahul.portfolio.contact;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Acceptance criteria FR3: valid submissions are stored and confirmed; invalid input shows field errors and keeps
 * the values; spam gets the normal confirmation without being stored.
 */
@WebMvcTest(ContactController.class)
class ContactControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private SpamGuard spamGuard;

	@MockitoBean
	private ContactService contactService;

	@Test
	void showsEmptyFormInGerman() throws Exception {
		assertThat(html(get("/de/contact"))).contains("<h1>Kontakt aufnehmen</h1>", "<label for=\"name\">Name</label>",
				"action=\"/de/contact\"", "aria-describedby=\"email-hint\"", "name=\"website\"",
				"<a href=\"/de/contact\" aria-current=\"page\">Kontakt</a>")
			.doesNotContain("role=\"alert\"");
	}

	/**
	 * Privacy notice: no cookies. Spring Security's public filter chain must not create a session or CSRF token.
	 */
	@Test
	void publicFormSetsNoCookie() throws Exception {
		MvcResult result = this.mvc.perform(get("/en/contact")).andExpect(status().isOk()).andReturn();

		assertThat(result.getResponse().getHeader("Set-Cookie")).isNull();
		assertThat(result.getRequest().getSession(false)).isNull();
		assertThat(result.getResponse().getContentAsString()).doesNotContain("_csrf");
	}

	@Test
	void validSubmissionIsStoredAndRedirectsToConfirmation() throws Exception {
		this.mvc.perform(valid(post("/en/contact")))
			.andExpect(status().isFound())
			.andExpect(redirectedUrl("/en/contact/sent"));

		then(this.contactService).should().submit(any(ContactForm.class));
	}

	@Test
	void invalidSubmissionShowsErrorsInLanguageAndKeepsValues() throws Exception {
		String html = html(post("/de/contact").param("name", "Anna Muster")
			.param("email", "not-an-address")
			.param("message", "   ")
			.param("website", ""));

		assertThat(html).contains("role=\"alert\"", "Bitte korrigieren Sie Folgendes:",
				"<a href=\"#email\">Bitte geben Sie eine gültige E-Mail-Adresse ein, zum Beispiel name@beispiel.ch.</a>",
				"<a href=\"#message\">Bitte schreiben Sie eine Nachricht.</a>", "value=\"Anna Muster\"",
				"value=\"not-an-address\"", "aria-invalid=\"true\"",
				"aria-describedby=\"email-hint email-error\"")
			.doesNotContain("<a href=\"#name\">");
		then(this.contactService).should(never()).submit(any());
	}

	@Test
	void spamGetsConfirmationButIsNotStored() throws Exception {
		given(this.spamGuard.isSpam(any(), anyString())).willReturn(true);

		this.mvc.perform(valid(post("/de/contact")).param("website", "https://spam.example"))
			.andExpect(status().isFound())
			.andExpect(redirectedUrl("/de/contact/sent"));

		then(this.contactService).should(never()).submit(any());
	}

	@Test
	void confirmationPage() throws Exception {
		assertThat(html(get("/en/contact/sent"))).contains("<h1>Thank you!</h1>", "Your message has arrived.");
	}

	private static MockHttpServletRequestBuilder valid(MockHttpServletRequestBuilder request) {
		return request.param("name", "Anna Muster")
			.param("email", "anna@example.ch")
			.param("message", "Hallo Rahul")
			.param("website", "");
	}

	private String html(MockHttpServletRequestBuilder request) throws Exception {
		String body = this.mvc.perform(request)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		return body.replaceAll("\\s+", " ");
	}

}
