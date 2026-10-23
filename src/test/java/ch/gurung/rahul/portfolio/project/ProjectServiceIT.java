package ch.gurung.rahul.portfolio.project;

import java.util.Locale;

import ch.gurung.rahul.portfolio.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Service against real PostgreSQL with the seeded projects (acceptance criteria FR1, FR2, FR6).
 */
@DataJpaTest
@Import({ ProjectService.class, TestcontainersConfiguration.class })
class ProjectServiceIT {

	@Autowired
	private ProjectService service;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void listsPublishedProjectsInSortOrderAndLanguage() {
		assertThat(this.service.publishedProjects(Locale.GERMAN)).extracting(ProjectSummary::title)
			.containsExactly("Notion-Projektmanagement-Board", "n8n-Workflow", "Lerndokumentations-Generator");
		assertThat(this.service.publishedProjects(Locale.ENGLISH)).extracting(ProjectSummary::title)
			.containsExactly("Notion project management board", "n8n workflow", "Learning documentation generator");
	}

	@Test
	void sortsTechnologiesIgnoringCase() {
		ProjectDetail project = this.service.publishedProject("n8n-workflow", Locale.ENGLISH).orElseThrow();

		assertThat(project.technologies()).containsExactly("Abacus", "Excel", "n8n");
	}

	@Test
	void returnsImagesOnceInSortOrderWithAltTextInLanguage() {
		long projectId = projectId("n8n-workflow");
		insertImage(projectId, "n8n-workflow/second.webp", "Second", "Zweites", 20);
		insertImage(projectId, "n8n-workflow/first.webp", "First", "Erstes", 10);
		this.entityManager.clear();

		ProjectDetail project = this.service.publishedProject("n8n-workflow", Locale.GERMAN).orElseThrow();

		assertThat(project.images()).containsExactly(new ProjectDetail.Image("n8n-workflow/first.webp", "Erstes"),
				new ProjectDetail.Image("n8n-workflow/second.webp", "Zweites"));
		assertThat(this.service.publishedProjects(Locale.ENGLISH)).filteredOn((p) -> p.slug().equals("n8n-workflow"))
			.singleElement()
			.extracting(ProjectSummary::cover)
			.isEqualTo(new ProjectDetail.Image("n8n-workflow/first.webp", "First"));
	}

	@Test
	void fallsBackToEnglishWhenTranslationIsMissing() {
		this.entityManager.getEntityManager()
			.createNativeQuery("insert into project (slug, status, sort_order, published) values ('english-only', 'PAST', 99, true)")
			.executeUpdate();
		this.entityManager.getEntityManager()
			.createNativeQuery("insert into project_translation (project_id, locale, title, summary, description) "
					+ "values (?, 'en', 'English only', 'Summary', 'Text')")
			.setParameter(1, projectId("english-only"))
			.executeUpdate();
		this.entityManager.clear();

		assertThat(this.service.publishedProject("english-only", Locale.GERMAN)).hasValueSatisfying(
				(project) -> assertThat(project.title()).isEqualTo("English only"));
	}

	@Test
	void hidesUnpublishedAndUnknownProjects() {
		this.entityManager.getEntityManager()
			.createNativeQuery("update project set published = false where slug = 'lerndoku-generator'")
			.executeUpdate();
		this.entityManager.clear();

		assertThat(this.service.publishedProjects(Locale.ENGLISH)).extracting(ProjectSummary::slug)
			.doesNotContain("lerndoku-generator");
		assertThat(this.service.publishedProject("lerndoku-generator", Locale.ENGLISH)).isEmpty();
		assertThat(this.service.publishedProject("does-not-exist", Locale.ENGLISH)).isEmpty();
	}

	private long projectId(String slug) {
		return ((Number) this.entityManager.getEntityManager()
			.createNativeQuery("select id from project where slug = ?")
			.setParameter(1, slug)
			.getSingleResult()).longValue();
	}

	private void insertImage(long projectId, String path, String altEn, String altDe, int sortOrder) {
		this.entityManager.getEntityManager()
			.createNativeQuery("insert into project_image (project_id, path, alt_en, alt_de, sort_order) values (?, ?, ?, ?, ?)")
			.setParameter(1, projectId)
			.setParameter(2, path)
			.setParameter(3, altEn)
			.setParameter(4, altDe)
			.setParameter(5, sortOrder)
			.executeUpdate();
	}

}
