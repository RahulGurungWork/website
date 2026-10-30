package ch.gurung.rahul.portfolio.project;

import java.time.LocalDate;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import ch.gurung.rahul.portfolio.technology.Technology;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

/**
 * A showcase project. Language-dependent text lives in {@link ProjectTranslation}.
 */
@Entity
public class Project {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String slug;

	@Enumerated(EnumType.STRING)
	private ProjectStatus status;

	private LocalDate startedOn;

	private LocalDate endedOn;

	private String repoUrl;

	private String demoUrl;

	private int sortOrder;

	private boolean published;

	@OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<ProjectTranslation> translations = new LinkedHashSet<>();

	@OneToMany(mappedBy = "project")
	@OrderBy("sortOrder")
	private Set<ProjectImage> images = new LinkedHashSet<>();

	@ManyToMany
	@JoinTable(name = "project_technology", joinColumns = @JoinColumn(name = "project_id"),
			inverseJoinColumns = @JoinColumn(name = "technology_id"))
	@OrderBy("name")
	private Set<Technology> technologies = new LinkedHashSet<>();

	protected Project() {
	}

	/**
	 * A new, unpublished project; the admin fills in the rest with {@link #update} and {@link #putTranslation}.
	 */
	public Project(String slug, int sortOrder) {
		this.slug = slug;
		this.status = ProjectStatus.CURRENT;
		this.sortOrder = sortOrder;
	}

	/**
	 * Sets everything except texts, technologies, images and order (admin panel, step A2).
	 */
	void update(String slug, ProjectStatus status, LocalDate startedOn, LocalDate endedOn, String repoUrl,
			String demoUrl, boolean published) {
		this.slug = slug;
		this.status = status;
		this.startedOn = startedOn;
		this.endedOn = endedOn;
		this.repoUrl = repoUrl;
		this.demoUrl = demoUrl;
		this.published = published;
	}

	void setPublished(boolean published) {
		this.published = published;
	}

	void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	/**
	 * Creates or replaces the texts in one language.
	 */
	void putTranslation(String language, String title, String summary, String description) {
		ProjectTranslation translation = translation(Locale.of(language)).orElseGet(() -> {
			ProjectTranslation created = new ProjectTranslation(this, language);
			this.translations.add(created);
			return created;
		});
		translation.update(title, summary, description);
	}

	void replaceTechnologies(Collection<Technology> technologies) {
		this.technologies.clear();
		this.technologies.addAll(technologies);
	}

	public Long getId() {
		return this.id;
	}

	public String getSlug() {
		return this.slug;
	}

	public ProjectStatus getStatus() {
		return this.status;
	}

	public LocalDate getStartedOn() {
		return this.startedOn;
	}

	public LocalDate getEndedOn() {
		return this.endedOn;
	}

	public String getRepoUrl() {
		return this.repoUrl;
	}

	public String getDemoUrl() {
		return this.demoUrl;
	}

	public int getSortOrder() {
		return this.sortOrder;
	}

	public boolean isPublished() {
		return this.published;
	}

	public Set<ProjectTranslation> getTranslations() {
		return this.translations;
	}

	public Set<ProjectImage> getImages() {
		return this.images;
	}

	public Set<Technology> getTechnologies() {
		return this.technologies;
	}

	/**
	 * Translation for the language of the given locale, if one exists.
	 */
	public Optional<ProjectTranslation> translation(Locale locale) {
		String language = locale.getLanguage();
		return this.translations.stream().filter((t) -> t.getLocale().equals(language)).findFirst();
	}

}
