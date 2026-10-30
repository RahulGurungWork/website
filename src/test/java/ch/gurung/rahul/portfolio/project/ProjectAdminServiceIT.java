package ch.gurung.rahul.portfolio.project;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import ch.gurung.rahul.portfolio.TestcontainersConfiguration;
import ch.gurung.rahul.portfolio.technology.Technology;
import ch.gurung.rahul.portfolio.technology.TechnologyRepository;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step A2 against real PostgreSQL with the seeded projects: what the admin saves is what the public pages show.
 */
@DataJpaTest
@Import({ ProjectAdminService.class, ProjectService.class, TestcontainersConfiguration.class })
class ProjectAdminServiceIT {

	@Autowired
	private ProjectAdminService admin;

	@Autowired
	private ProjectService site;

	@Autowired
	private TechnologyRepository technologies;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void publishedNewProjectAppearsLastOnTheSiteInBothLanguages() {
		long python = this.technologies.findByName("Python").map(Technology::getId).orElseThrow();
		ProjectForm form = form("portfolio-website", true);
		form.setTechnologyIds(List.of(python));
		form.setNewTechnologies("Spring Boot, docker , python");

		long id = this.admin.create(form);
		reload();

		assertThat(this.site.publishedProjects(Locale.GERMAN)).last()
			.satisfies((summary) -> assertThat(summary.title()).isEqualTo("Portfolio-Website"));
		ProjectDetail detail = this.site.publishedProject("portfolio-website", Locale.ENGLISH).orElseThrow();
		assertThat(detail.title()).isEqualTo("Portfolio website");
		assertThat(detail.paragraphs()).containsExactly("First paragraph.", "Second paragraph.");
		assertThat(detail.technologies()).containsExactly("docker", "Python", "Spring Boot");
		assertThat(this.technologies.findAll()).filteredOn((t) -> t.getName().equalsIgnoreCase("python")).hasSize(1);
		assertThat(this.admin.list(Locale.ENGLISH)).last().satisfies((row) -> {
			assertThat(row.id()).isEqualTo(id);
			assertThat(row.last()).isTrue();
		});
	}

	@Test
	void unpublishedProjectStaysOffTheSite() {
		this.admin.create(form("draft-project", false));
		reload();

		assertThat(this.site.publishedProject("draft-project", Locale.ENGLISH)).isEmpty();
		assertThat(this.admin.list(Locale.ENGLISH)).extracting(ProjectAdminService.Row::slug).contains("draft-project");
	}

	@Test
	void editingChangesTextsAndFormShowsCurrentValues() {
		long id = idOf("n8n-workflow");
		ProjectForm form = this.admin.form(id).orElseThrow();
		assertThat(form.getTitleDe()).isEqualTo("n8n-Workflow");

		form.setTitleDe("n8n-Workflow für Abacus");
		form.setEndedOn(LocalDate.of(2026, 9, 30));
		assertThat(this.admin.update(id, form)).isTrue();
		reload();

		assertThat(this.site.publishedProject("n8n-workflow", Locale.GERMAN).orElseThrow().title())
			.isEqualTo("n8n-Workflow für Abacus");
		assertThat(this.admin.form(id).orElseThrow().getEndedOn()).isEqualTo(LocalDate.of(2026, 9, 30));
	}

	@Test
	void hidingRemovesProjectFromTheSite() {
		this.admin.setPublished(idOf("lerndoku-generator"), false);
		reload();

		assertThat(this.site.publishedProject("lerndoku-generator", Locale.ENGLISH)).isEmpty();
	}

	@Test
	void movingUpSwapsWithTheProjectBefore() {
		List<String> before = slugs();

		this.admin.move(idOf(before.get(1)), true);
		reload();

		assertThat(slugs()).containsExactly(before.get(1), before.get(0), before.get(2));
		assertThat(this.site.publishedProjects(Locale.ENGLISH)).extracting(ProjectSummary::slug)
			.startsWith(before.get(1), before.get(0));
	}

	@Test
	void movingBeyondTheEndsChangesNothing() {
		List<String> before = slugs();

		this.admin.move(idOf(before.get(0)), true);
		this.admin.move(idOf(before.get(before.size() - 1)), false);
		reload();

		assertThat(slugs()).isEqualTo(before);
	}

	@Test
	void deletingRemovesProjectButKeepsTechnologies() {
		long technologiesBefore = this.technologies.count();

		this.admin.delete(idOf("n8n-workflow"));
		reload();

		assertThat(slugs()).doesNotContain("n8n-workflow");
		assertThat(this.site.publishedProject("n8n-workflow", Locale.ENGLISH)).isEmpty();
		assertThat(this.technologies.count()).isEqualTo(technologiesBefore);
	}

	@Test
	void knowsWhichSlugsAreTaken() {
		long id = idOf("n8n-workflow");

		assertThat(this.admin.slugTaken("n8n-workflow", null)).isTrue();
		assertThat(this.admin.slugTaken("n8n-workflow", id)).isFalse();
		assertThat(this.admin.slugTaken("something-new", null)).isFalse();
	}

	private static ProjectForm form(String slug, boolean published) {
		ProjectForm form = new ProjectForm();
		form.setSlug(slug);
		form.setStatus(ProjectStatus.CURRENT);
		form.setStartedOn(LocalDate.of(2026, 10, 1));
		form.setPublished(published);
		form.setTitleEn("Portfolio website");
		form.setSummaryEn("This website.");
		form.setDescriptionEn("First paragraph.\n\nSecond paragraph.");
		form.setTitleDe("Portfolio-Website");
		form.setSummaryDe("Diese Website.");
		form.setDescriptionDe("Erster Absatz.\n\nZweiter Absatz.");
		return form;
	}

	private long idOf(String slug) {
		return this.admin.list(Locale.ENGLISH)
			.stream()
			.filter((row) -> row.slug().equals(slug))
			.findFirst()
			.orElseThrow()
			.id();
	}

	private List<String> slugs() {
		return this.admin.list(Locale.ENGLISH).stream().map(ProjectAdminService.Row::slug).toList();
	}

	private void reload() {
		this.entityManager.flush();
		this.entityManager.clear();
	}

}
