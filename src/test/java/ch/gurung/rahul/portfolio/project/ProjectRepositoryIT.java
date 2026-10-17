package ch.gurung.rahul.portfolio.project;

import java.util.List;
import java.util.Locale;

import ch.gurung.rahul.portfolio.TestcontainersConfiguration;
import ch.gurung.rahul.portfolio.technology.Technology;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the Flyway migrations against real PostgreSQL and checks the seeded projects and queries.
 */
@DataJpaTest
@Import(TestcontainersConfiguration.class)
class ProjectRepositoryIT {

	@Autowired
	private ProjectRepository projects;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void listsSeededProjectsInSortOrder() {
		List<Project> published = this.projects.findByPublishedTrueOrderBySortOrderAsc();

		assertThat(published).extracting(Project::getSlug)
			.containsExactly("notion-workspace-prototype", "n8n-workflow", "lerndoku-generator");
	}

	@Test
	void everyPublishedProjectHasEnglishAndGermanTranslation() {
		for (Project project : this.projects.findByPublishedTrueOrderBySortOrderAsc()) {
			assertThat(project.getTranslations()).as(project.getSlug())
				.extracting(ProjectTranslation::getLocale)
				.containsExactlyInAnyOrder("en", "de");
		}
	}

	@Test
	void neverListsUnpublishedProjects() {
		insertProject("hidden-project", false);

		assertThat(this.projects.findByPublishedTrueOrderBySortOrderAsc()).extracting(Project::getSlug)
			.doesNotContain("hidden-project");
		assertThat(this.projects.findBySlugAndPublishedTrue("hidden-project")).isEmpty();
	}

	@Test
	void findsPublishedProjectBySlugWithDetails() {
		Project project = this.projects.findBySlugAndPublishedTrue("n8n-workflow").orElseThrow();

		assertThat(project.getStatus()).isEqualTo(ProjectStatus.CURRENT);
		assertThat(project.getTechnologies()).extracting(Technology::getName)
			.containsExactlyInAnyOrder("n8n", "REST API");
		assertThat(project.translation(Locale.GERMAN)).hasValueSatisfying(
				(translation) -> assertThat(translation.getTitle()).isEqualTo("n8n-Workflow"));
		assertThat(project.translation(Locale.forLanguageTag("en-CH"))).hasValueSatisfying(
				(translation) -> assertThat(translation.getTitle()).isEqualTo("n8n workflow"));
	}

	@Test
	void unknownSlugIsEmpty() {
		assertThat(this.projects.findBySlugAndPublishedTrue("does-not-exist")).isEmpty();
	}

	private void insertProject(String slug, boolean published) {
		this.entityManager.getEntityManager()
			.createNativeQuery("insert into project (slug, status, sort_order, published) values (?, 'PAST', 99, ?)")
			.setParameter(1, slug)
			.setParameter(2, published)
			.executeUpdate();
	}

}
