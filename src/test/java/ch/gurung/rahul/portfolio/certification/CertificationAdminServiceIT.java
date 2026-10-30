package ch.gurung.rahul.portfolio.certification;

import java.time.LocalDate;
import java.util.List;

import ch.gurung.rahul.portfolio.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step A3 against real PostgreSQL with the seeded certifications: what the admin saves is what the certificates page
 * shows, in the same groups and order.
 */
@DataJpaTest
@Import({ CertificationAdminService.class, CertificationService.class, TestcontainersConfiguration.class })
class CertificationAdminServiceIT {

	@Autowired
	private CertificationAdminService admin;

	@Autowired
	private CertificationService site;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void listIsGroupedLikeThePage() {
		assertThat(this.admin.list()).extracting(CertificationAdminService.Group::category)
			.containsExactly(CertificationCategory.LANGUAGE_AND_SCHOOL, CertificationCategory.TECHNICAL,
					CertificationCategory.PROFESSIONAL);
		assertThat(names(CertificationCategory.TECHNICAL)).isEqualTo(siteNames(CertificationCategory.TECHNICAL));
	}

	@Test
	void newCertificationAppearsAtTheEndOfItsGroup() {
		CertificationForm form = form("Learn Java", CertificationCategory.TECHNICAL);
		form.setCredentialUrl("https://example.com/cert/123");

		this.admin.create(form);
		reload();

		assertThat(siteNames(CertificationCategory.TECHNICAL)).last().isEqualTo("Learn Java");
		CertificationEntry entry = entry("Learn Java");
		assertThat(entry.credentialUrl()).isEqualTo("https://example.com/cert/123");
		assertThat(entry.issuedOn()).isEqualTo(LocalDate.of(2026, 10, 1));
	}

	@Test
	void editingCanMoveToAnotherGroupAndClearTheLink() {
		long id = idOf("Learn Bootstrap");
		CertificationForm form = this.admin.form(id).orElseThrow();
		form.setCategory(CertificationCategory.PROFESSIONAL);
		form.setCredentialUrl("");

		assertThat(this.admin.update(id, form)).isTrue();
		reload();

		assertThat(siteNames(CertificationCategory.TECHNICAL)).doesNotContain("Learn Bootstrap");
		assertThat(siteNames(CertificationCategory.PROFESSIONAL)).contains("Learn Bootstrap");
		assertThat(entry("Learn Bootstrap").credentialUrl()).isNull();
	}

	@Test
	void movingStaysWithinTheGroup() {
		List<String> technical = names(CertificationCategory.TECHNICAL);
		List<String> languages = names(CertificationCategory.LANGUAGE_AND_SCHOOL);

		this.admin.move(idOf(technical.get(1)), true);
		reload();

		assertThat(siteNames(CertificationCategory.TECHNICAL)).startsWith(technical.get(1), technical.get(0));
		assertThat(siteNames(CertificationCategory.LANGUAGE_AND_SCHOOL)).isEqualTo(languages);
	}

	@Test
	void movingBeyondTheGroupEndsChangesNothing() {
		List<String> technical = names(CertificationCategory.TECHNICAL);

		this.admin.move(idOf(technical.get(0)), true);
		this.admin.move(idOf(technical.get(technical.size() - 1)), false);
		reload();

		assertThat(siteNames(CertificationCategory.TECHNICAL)).isEqualTo(technical);
	}

	@Test
	void deletingRemovesItFromThePage() {
		this.admin.delete(idOf("Learn Bash Scripting"));
		reload();

		assertThat(siteNames(CertificationCategory.TECHNICAL)).doesNotContain("Learn Bash Scripting");
	}

	private static CertificationForm form(String name, CertificationCategory category) {
		CertificationForm form = new CertificationForm();
		form.setName(name);
		form.setIssuer("Codecademy");
		form.setIssuedOn(LocalDate.of(2026, 10, 1));
		form.setCategory(category);
		return form;
	}

	private List<String> names(CertificationCategory category) {
		return this.admin.list()
			.stream()
			.filter((group) -> group.category() == category)
			.flatMap((group) -> group.rows().stream())
			.map(CertificationAdminService.Row::name)
			.toList();
	}

	private List<String> siteNames(CertificationCategory category) {
		return this.site.groupedCertifications()
			.stream()
			.filter((group) -> group.category() == category)
			.flatMap((group) -> group.entries().stream())
			.map(CertificationEntry::name)
			.toList();
	}

	private CertificationEntry entry(String name) {
		return this.site.certifications().stream().filter((e) -> e.name().equals(name)).findFirst().orElseThrow();
	}

	private long idOf(String name) {
		return this.admin.list()
			.stream()
			.flatMap((group) -> group.rows().stream())
			.filter((row) -> row.name().equals(name))
			.findFirst()
			.orElseThrow()
			.id();
	}

	private void reload() {
		this.entityManager.flush();
		this.entityManager.clear();
	}

}
