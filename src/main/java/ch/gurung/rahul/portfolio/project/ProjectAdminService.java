package ch.gurung.rahul.portfolio.project;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import ch.gurung.rahul.portfolio.common.Languages;
import ch.gurung.rahul.portfolio.technology.Technology;
import ch.gurung.rahul.portfolio.technology.TechnologyRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Project management for the admin panel (step A2). Changes show on the public pages at once, because those read the
 * same tables.
 */
@Service
@Transactional
public class ProjectAdminService {

	/**
	 * Gap between the {@code sort_order} of new projects, as in the seed migrations.
	 */
	static final int SORT_STEP = 10;

	private final ProjectRepository projects;

	private final TechnologyRepository technologies;

	public ProjectAdminService(ProjectRepository projects, TechnologyRepository technologies) {
		this.projects = projects;
		this.technologies = technologies;
	}

	/**
	 * One row of the admin list.
	 *
	 * @param id project id
	 * @param slug URL part
	 * @param title title in the admin's language, English if that is missing
	 * @param status current or past
	 * @param published shown on the public site
	 * @param first first in the order, so it cannot move up
	 * @param last last in the order, so it cannot move down
	 */
	public record Row(long id, String slug, String title, ProjectStatus status, boolean published, boolean first,
			boolean last) {
	}

	@Transactional(readOnly = true)
	public List<Row> list(Locale locale) {
		List<Project> all = this.projects.findAllByOrderBySortOrderAscIdAsc();
		List<Row> rows = new ArrayList<>(all.size());
		for (int i = 0; i < all.size(); i++) {
			Project project = all.get(i);
			rows.add(new Row(project.getId(), project.getSlug(), title(project, locale), project.getStatus(),
					project.isPublished(), i == 0, i == all.size() - 1));
		}
		return rows;
	}

	/**
	 * The form filled with a project's current values.
	 */
	@Transactional(readOnly = true)
	public Optional<ProjectForm> form(long id) {
		return this.projects.findWithDetailsById(id).map((project) -> {
			ProjectForm form = new ProjectForm();
			form.setSlug(project.getSlug());
			form.setStatus(project.getStatus());
			form.setStartedOn(project.getStartedOn());
			form.setEndedOn(project.getEndedOn());
			form.setRepoUrl(project.getRepoUrl());
			form.setDemoUrl(project.getDemoUrl());
			form.setPublished(project.isPublished());
			form.setTechnologyIds(project.getTechnologies().stream().map(Technology::getId).toList());
			project.translation(Locale.ENGLISH).ifPresent((t) -> {
				form.setTitleEn(t.getTitle());
				form.setSummaryEn(t.getSummary());
				form.setDescriptionEn(t.getDescription());
			});
			project.translation(Locale.GERMAN).ifPresent((t) -> {
				form.setTitleDe(t.getTitle());
				form.setSummaryDe(t.getSummary());
				form.setDescriptionDe(t.getDescription());
			});
			return form;
		});
	}

	/**
	 * All technologies for the checkboxes, sorted ignoring case.
	 */
	@Transactional(readOnly = true)
	public List<Technology> technologies() {
		return this.technologies.findAll()
			.stream()
			.sorted(Comparator.comparing(Technology::getName, String.CASE_INSENSITIVE_ORDER))
			.toList();
	}

	/**
	 * Whether another project already uses {@code slug}.
	 *
	 * @param id the project being edited, or {@code null} for a new one
	 */
	@Transactional(readOnly = true)
	public boolean slugTaken(String slug, Long id) {
		return (id != null) ? this.projects.existsBySlugAndIdNot(slug, id) : this.projects.existsBySlug(slug);
	}

	/**
	 * Saves a new project at the end of the order and returns its id.
	 */
	public long create(ProjectForm form) {
		Project project = new Project(form.getSlug(), this.projects.maxSortOrder() + SORT_STEP);
		apply(project, form);
		return this.projects.save(project).getId();
	}

	/**
	 * @return false if there is no project with this id
	 */
	public boolean update(long id, ProjectForm form) {
		Optional<Project> project = this.projects.findWithDetailsById(id);
		project.ifPresent((p) -> apply(p, form));
		return project.isPresent();
	}

	public boolean setPublished(long id, boolean published) {
		Optional<Project> project = this.projects.findById(id);
		project.ifPresent((p) -> p.setPublished(published));
		return project.isPresent();
	}

	/**
	 * Swaps the project with its neighbour; does nothing at either end of the list.
	 *
	 * @param up towards the start of the list (shown first on the site)
	 */
	public void move(long id, boolean up) {
		List<Project> all = this.projects.findAllByOrderBySortOrderAscIdAsc();
		int index = -1;
		for (int i = 0; i < all.size(); i++) {
			if (all.get(i).getId() == id) {
				index = i;
			}
		}
		int other = up ? index - 1 : index + 1;
		if (index < 0 || other < 0 || other >= all.size()) {
			return;
		}
		// Renumber everything so equal sort orders from older data cannot make a swap a no-op
		for (int i = 0; i < all.size(); i++) {
			all.get(i).setSortOrder((i + 1) * SORT_STEP);
		}
		Project a = all.get(index);
		Project b = all.get(other);
		int sortOrder = a.getSortOrder();
		a.setSortOrder(b.getSortOrder());
		b.setSortOrder(sortOrder);
	}

	@Transactional(readOnly = true)
	public Optional<String> title(long id, Locale locale) {
		return this.projects.findWithDetailsById(id).map((project) -> title(project, locale));
	}

	/**
	 * Deletes the project with its texts, images and technology links (the database cascades); the technologies
	 * themselves stay.
	 */
	public void delete(long id) {
		this.projects.findById(id).ifPresent(this.projects::delete);
	}

	private void apply(Project project, ProjectForm form) {
		project.update(form.getSlug(), form.getStatus(), form.getStartedOn(), form.getEndedOn(),
				emptyToNull(form.getRepoUrl()), emptyToNull(form.getDemoUrl()), form.isPublished());
		project.putTranslation("en", form.getTitleEn().strip(), form.getSummaryEn().strip(),
				form.getDescriptionEn().strip());
		project.putTranslation("de", form.getTitleDe().strip(), form.getSummaryDe().strip(),
				form.getDescriptionDe().strip());
		project.replaceTechnologies(chosenTechnologies(form));
	}

	/**
	 * The ticked technologies plus the ones typed in (comma-separated); a typed name that exists, ignoring case, is
	 * reused instead of created twice.
	 */
	private List<Technology> chosenTechnologies(ProjectForm form) {
		Map<String, Technology> chosen = new LinkedHashMap<>();
		for (Technology technology : this.technologies.findAllById(form.getTechnologyIds())) {
			chosen.put(technology.getName().toLowerCase(Locale.ROOT), technology);
		}
		for (String name : form.getNewTechnologies().split(",")) {
			String clean = name.strip();
			if (clean.isEmpty() || clean.length() > 100 || chosen.containsKey(clean.toLowerCase(Locale.ROOT))) {
				continue;
			}
			Technology technology = this.technologies.findByNameIgnoreCase(clean)
				.orElseGet(() -> this.technologies.save(new Technology(clean)));
			chosen.put(clean.toLowerCase(Locale.ROOT), technology);
		}
		return new ArrayList<>(chosen.values());
	}

	private static String title(Project project, Locale locale) {
		return project.translation(locale)
			.or(() -> project.translation(Locale.of(Languages.DEFAULT)))
			.map(ProjectTranslation::getTitle)
			.orElse(project.getSlug());
	}

	private static String emptyToNull(String value) {
		return (value == null || value.isBlank()) ? null : value;
	}

}
