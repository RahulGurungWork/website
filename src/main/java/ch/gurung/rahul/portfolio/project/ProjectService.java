package ch.gurung.rahul.portfolio.project;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import ch.gurung.rahul.portfolio.common.Languages;
import ch.gurung.rahul.portfolio.technology.Technology;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Published projects in the visitor's language. Unpublished projects are never returned.
 */
@Service
@Transactional(readOnly = true)
public class ProjectService {

	private final ProjectRepository projects;

	public ProjectService(ProjectRepository projects) {
		this.projects = projects;
	}

	/**
	 * Published projects in {@code sort_order}. A project without a translation in the requested language falls
	 * back to English.
	 */
	public List<ProjectSummary> publishedProjects(Locale locale) {
		return this.projects.findByPublishedTrueOrderBySortOrderAsc()
			.stream()
			.flatMap((project) -> translation(project, locale).map((t) -> new ProjectSummary(project.getSlug(),
					t.getTitle(), t.getSummary(), project.getStatus(), technologyNames(project),
					images(project, locale).stream().findFirst().orElse(null)))
				.stream())
			.toList();
	}

	/**
	 * One published project, or empty if the slug is unknown or the project is not published.
	 */
	public Optional<ProjectDetail> publishedProject(String slug, Locale locale) {
		return this.projects.findBySlugAndPublishedTrue(slug)
			.flatMap((project) -> translation(project, locale).map((t) -> new ProjectDetail(project.getSlug(),
					t.getTitle(), t.getSummary(), t.getDescription(), project.getStatus(), project.getStartedOn(),
					project.getEndedOn(), project.getRepoUrl(), project.getDemoUrl(), technologyNames(project),
					images(project, locale))));
	}

	private static List<ProjectDetail.Image> images(Project project, Locale locale) {
		return project.getImages()
			.stream()
			.map((image) -> new ProjectDetail.Image(image.getPath(), image.alt(locale)))
			.toList();
	}

	private static Optional<ProjectTranslation> translation(Project project, Locale locale) {
		return project.translation(locale).or(() -> project.translation(Locale.of(Languages.DEFAULT)));
	}

	/**
	 * Technology names sorted for display, ignoring case (PostgreSQL would put "REST API" before "n8n").
	 */
	private static List<String> technologyNames(Project project) {
		return project.getTechnologies()
			.stream()
			.map(Technology::getName)
			.sorted(String.CASE_INSENSITIVE_ORDER)
			.toList();
	}

}
