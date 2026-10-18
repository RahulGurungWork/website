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
 * Service against real PostgreSQL with the certifications seeded by V3 and grouped by V5.
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
					"Cambridge University Press & Assessment", LocalDate.of(2026, 7, 19), null,
					CertificationCategory.LANGUAGE_AND_SCHOOL));
	}

	@Test
	void groupsSeededCertificationsInCategoryOrder() {
		List<CertificationEntry.Group> groups = this.service.groupedCertifications();

		assertThat(groups).extracting(CertificationEntry.Group::category)
			.containsExactly(CertificationCategory.LANGUAGE_AND_SCHOOL, CertificationCategory.TECHNICAL,
					CertificationCategory.PROFESSIONAL);
		assertThat(groups.get(0).entries()).extracting(CertificationEntry::name)
			.containsExactly("C1 Advanced (Certificate in Advanced English)", "English course, level C1 (52 lessons)");
		assertThat(groups.get(1).entries()).extracting(CertificationEntry::name)
			.containsExactly("Learn GitHub: Introduction", "Introduction to UI and UX Design",
					"Operating Systems: Introduction", "Intro to Generative AI", "Learn Bootstrap",
					"Learn Bash Scripting");
		assertThat(groups.get(2).entries()).hasSize(18)
			.first()
			.extracting(CertificationEntry::name)
			.isEqualTo("Lead Link, Kreis BI-Lernende");
	}

	@Test
	void everyCertificationHasNameIssuerAndCategory() {
		assertThat(this.service.certifications()).allSatisfy((certification) -> {
			assertThat(certification.name()).isNotBlank();
			assertThat(certification.issuer()).isNotBlank();
			assertThat(certification.category()).isNotNull();
		});
	}

}
