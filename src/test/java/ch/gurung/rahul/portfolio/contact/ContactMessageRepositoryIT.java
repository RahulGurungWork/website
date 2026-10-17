package ch.gurung.rahul.portfolio.contact;

import ch.gurung.rahul.portfolio.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class ContactMessageRepositoryIT {

	@Autowired
	private ContactMessageRepository messages;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void storesMessageAsNotMailedUntilMarked() {
		ContactMessage message = this.messages.save(new ContactMessage("Anna Muster", "anna@example.ch", "Hallo"));
		this.entityManager.flush();
		this.entityManager.clear();

		assertThat(this.messages.findByMailSentFalseOrderByCreatedAtAsc()).singleElement().satisfies((stored) -> {
			assertThat(stored.getEmail()).isEqualTo("anna@example.ch");
			assertThat(stored.getCreatedAt()).isNotNull();
		});

		ContactMessage stored = this.messages.findById(message.getId()).orElseThrow();
		stored.markMailSent();
		this.entityManager.flush();

		assertThat(this.messages.findByMailSentFalseOrderByCreatedAtAsc()).isEmpty();
	}

}
