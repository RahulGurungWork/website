package ch.gurung.rahul.portfolio.certification;

import java.time.LocalDate;
import java.util.List;

import ch.gurung.rahul.portfolio.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Service against real PostgreSQL with the certifications seeded by V3.
 */
@DataJpaTest
@Import({ CertificationService.class, TestcontainersConfiguration.class })
class CertificationServiceIT {

	@Autowired
	private CertificationService service;

	@Test
	void listsSeededCertificationsInSortOrder() {
		List<CertificationEntry> certifications = this.service.certifications();

		assertThat(certifications).hasSize(26);
		assertThat(certifications).first()
			.isEqualTo(new CertificationEntry("C1 Advanced (Certificate in Advanced English)",
					"Cambridge University Press & Assessment", LocalDate.of(2026, 7, 19), null));
		assertThat(certifications).extracting(CertificationEntry::name)
			.containsSubsequence("English course, level C1 (52 lessons)", "Lead Link, Kreis BI-Lernende",
					"Learn GitHub: Introduction", "Learn Bash Scripting", "Set Your Workspace Up For Success",
					"Learn What to Learn");
	}

	@Test
	void everyCertificationHasNameAndIssuer() {
		assertThat(this.service.certifications()).allSatisfy((certification) -> {
			assertThat(certification.name()).isNotBlank();
			assertThat(certification.issuer()).isNotBlank();
		});
	}

}
