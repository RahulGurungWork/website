package ch.gurung.rahul.portfolio.contact;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
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
 * Step A4: the inbox needs a login, shows unread messages in words, escapes what visitors wrote, and changes data only
 * through POST with a CSRF token.
 */
@WebMvcTest(MessageAdminController.class)
class MessageAdminControllerTest {

	private static final ZonedDateTime RECEIVED = ZonedDateTime.parse("2026-10-30T14:05:00+01:00[Europe/Zurich]");

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private MessageAdminService service;

	@Test
	void loggedOutVisitorsAreSentToLogin() throws Exception {
		this.mvc.perform(get("/de/admin/messages")).andExpect(redirectedUrl("/de/admin/login"));
		this.mvc.perform(get("/en/admin/messages/1")).andExpect(redirectedUrl("/en/admin/login"));
		then(this.service).should(never()).open(anyLong());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void inboxShowsNewMessagesInWords() throws Exception {
		given(this.service.list()).willReturn(List.of(
				new MessageAdminService.Row(2, "Ben Beispiel", RECEIVED, "Hello", false, false),
				new MessageAdminService.Row(1, "Anna Muster", RECEIVED, "Hi", true, true)));
		given(this.service.unreadCount()).willReturn(1L);

		String html = html(get("/de/admin/messages"));

		assertThat(html).contains("<h1>Nachrichten</h1>", "Ungelesen: 1.", "class=\"admin-unread\"", ">Neu<",
				">Gelesen<", "Fehlgeschlagen", "href=\"/de/admin/messages/2\"", ">30.10.2026 14:05<",
				"aria-current=\"page\">Nachrichten</a>");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void messageIsShownEscapedWithReplyLink() throws Exception {
		given(this.service.open(5)).willReturn(Optional.of(new MessageAdminService.Detail(5, "Anna Muster",
				"anna@example.ch", "<script>alert(1)</script>\nSecond line", RECEIVED, true)));

		String html = html(get("/en/admin/messages/5"));

		assertThat(html).contains("<h1>Message from Anna Muster</h1>", "href=\"mailto:anna@example.ch\"",
				"&lt;script&gt;alert(1)&lt;/script&gt;", "action=\"/en/admin/messages/5/unread\"")
			.doesNotContain("<script>alert(1)</script>");
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void unknownMessageIsNotFound() throws Exception {
		given(this.service.open(99)).willReturn(Optional.empty());
		given(this.service.markUnread(99)).willReturn(false);

		this.mvc.perform(get("/en/admin/messages/99")).andExpect(status().isNotFound());
		this.mvc.perform(post("/en/admin/messages/99/unread").with(csrf())).andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void markUnreadRedirectsWithNotice() throws Exception {
		given(this.service.markUnread(5)).willReturn(true);

		this.mvc.perform(post("/de/admin/messages/5/unread").with(csrf()))
			.andExpect(redirectedUrl("/de/admin/messages"))
			.andExpect(flash().attribute("notice", "admin.message.markedUnread"));
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void deleteAsksFirstAndNeedsCsrf() throws Exception {
		given(this.service.sender(5)).willReturn(Optional.of("Anna Muster"));

		assertThat(html(get("/de/admin/messages/5/delete"))).contains("<h1>Nachricht löschen?</h1>",
				"Die Nachricht von Anna Muster wird gelöscht.");
		this.mvc.perform(post("/de/admin/messages/5/delete")).andExpect(status().isForbidden());
		then(this.service).should(never()).delete(anyLong());

		this.mvc.perform(post("/de/admin/messages/5/delete").with(csrf()))
			.andExpect(redirectedUrl("/de/admin/messages"));
		then(this.service).should().delete(5L);
	}

	private String html(MockHttpServletRequestBuilder request) throws Exception {
		return this.mvc.perform(request).andReturn().getResponse().getContentAsString();
	}

}
