package ch.gurung.rahul.portfolio.contact;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import ch.gurung.rahul.portfolio.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step A4 against real PostgreSQL (with migration V7): the inbox lists newest first, opening marks as read, and the
 * admin can mark unread or delete.
 */
@DataJpaTest
@Import({ MessageAdminService.class, TestcontainersConfiguration.class, MessageAdminServiceIT.FixedClock.class })
class MessageAdminServiceIT {

	static final Instant NOW = Instant.parse("2026-10-30T13:05:00Z");

	@Autowired
	private MessageAdminService admin;

	@Autowired
	private ContactMessageRepository messages;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void listsNewestFirstAsUnreadWithShortPreview() {
		long older = save("Anna Muster", "Hello,\n\nI saw your n8n workflow.");
		long newer = save("Ben Beispiel", "x".repeat(200));
		reload();

		assertThat(this.admin.list()).extracting(MessageAdminService.Row::id).containsExactly(newer, older);
		MessageAdminService.Row anna = this.admin.list().get(1);
		assertThat(anna.preview()).isEqualTo("Hello, I saw your n8n workflow.");
		assertThat(anna.read()).isFalse();
		assertThat(this.admin.list().get(0).preview()).hasSize(MessageAdminService.PREVIEW_LENGTH).endsWith("…");
		assertThat(this.admin.unreadCount()).isEqualTo(2);
	}

	@Test
	void openingMarksAsReadAndShowsSwissTime() {
		long id = save("Anna Muster", "Hello");
		reload();

		MessageAdminService.Detail detail = this.admin.open(id).orElseThrow();
		reload();

		assertThat(detail.email()).isEqualTo("anna@example.ch");
		assertThat(detail.receivedAt().getZone()).hasToString("Europe/Zurich");
		assertThat(this.messages.findById(id).orElseThrow().getReadAt()).isEqualTo(NOW);
		assertThat(this.admin.unreadCount()).isZero();
	}

	@Test
	void canBeMarkedUnreadAgain() {
		long id = save("Anna Muster", "Hello");
		this.admin.open(id);
		reload();

		assertThat(this.admin.markUnread(id)).isTrue();
		reload();

		assertThat(this.admin.list().get(0).read()).isFalse();
		assertThat(this.admin.markUnread(9999)).isFalse();
	}

	@Test
	void deleteRemovesTheMessage() {
		long id = save("Anna Muster", "Hello");
		reload();

		this.admin.delete(id);
		reload();

		assertThat(this.admin.list()).isEmpty();
		assertThat(this.admin.open(id)).isEmpty();
	}

	private long save(String name, String text) {
		ContactMessage message = new ContactMessage(name, "anna@example.ch", text);
		return this.messages.saveAndFlush(message).getId();
	}

	private void reload() {
		this.entityManager.flush();
		this.entityManager.clear();
	}

	@TestConfiguration(proxyBeanMethods = false)
	static class FixedClock {

		@Bean
		Clock clock() {
			return Clock.fixed(NOW, ZoneOffset.UTC);
		}

	}

}
