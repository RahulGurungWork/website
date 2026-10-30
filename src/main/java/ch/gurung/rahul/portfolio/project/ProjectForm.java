package ch.gurung.rahul.portfolio.project;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * What the admin project form posts (step A2). Both languages are required, so every published project has EN and DE
 * texts (FR6). Messages in braces are keys in {@code i18n/messages*.properties}. Patterns accept {@code ""} so an empty
 * required field reports only "required".
 */
public class ProjectForm {

	@NotBlank(message = "{admin.form.required}")
	@Size(max = 100, message = "{admin.project.slug.tooLong}")
	@Pattern(regexp = "|[a-z0-9]+(-[a-z0-9]+)*", message = "{admin.project.slug.invalid}")
	private String slug = "";

	@NotNull(message = "{admin.form.required}")
	private ProjectStatus status = ProjectStatus.CURRENT;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate startedOn;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate endedOn;

	@Size(max = 500, message = "{admin.project.url.tooLong}")
	@Pattern(regexp = "|https?://\\S+", message = "{admin.project.url.invalid}")
	private String repoUrl = "";

	@Size(max = 500, message = "{admin.project.url.tooLong}")
	@Pattern(regexp = "|https?://\\S+", message = "{admin.project.url.invalid}")
	private String demoUrl = "";

	private boolean published;

	private List<Long> technologyIds = new ArrayList<>();

	@Size(max = 300, message = "{admin.project.newTechnologies.tooLong}")
	private String newTechnologies = "";

	@NotBlank(message = "{admin.form.required}")
	@Size(max = 200, message = "{admin.project.title.tooLong}")
	private String titleEn = "";

	@NotBlank(message = "{admin.form.required}")
	@Size(max = 500, message = "{admin.project.summary.tooLong}")
	private String summaryEn = "";

	@NotBlank(message = "{admin.form.required}")
	@Size(max = 20000, message = "{admin.project.description.tooLong}")
	private String descriptionEn = "";

	@NotBlank(message = "{admin.form.required}")
	@Size(max = 200, message = "{admin.project.title.tooLong}")
	private String titleDe = "";

	@NotBlank(message = "{admin.form.required}")
	@Size(max = 500, message = "{admin.project.summary.tooLong}")
	private String summaryDe = "";

	@NotBlank(message = "{admin.form.required}")
	@Size(max = 20000, message = "{admin.project.description.tooLong}")
	private String descriptionDe = "";

	/**
	 * The fields in the order the form shows them, for the error summary.
	 */
	public static List<String> fieldOrder() {
		return List.of("slug", "status", "startedOn", "endedOn", "repoUrl", "demoUrl", "newTechnologies", "titleEn",
				"summaryEn", "descriptionEn", "titleDe", "summaryDe", "descriptionDe");
	}

	public String getSlug() {
		return this.slug;
	}

	public void setSlug(String slug) {
		this.slug = (slug != null) ? slug.strip() : "";
	}

	public ProjectStatus getStatus() {
		return this.status;
	}

	public void setStatus(ProjectStatus status) {
		this.status = status;
	}

	public LocalDate getStartedOn() {
		return this.startedOn;
	}

	public void setStartedOn(LocalDate startedOn) {
		this.startedOn = startedOn;
	}

	public LocalDate getEndedOn() {
		return this.endedOn;
	}

	public void setEndedOn(LocalDate endedOn) {
		this.endedOn = endedOn;
	}

	public String getRepoUrl() {
		return this.repoUrl;
	}

	public void setRepoUrl(String repoUrl) {
		this.repoUrl = (repoUrl != null) ? repoUrl.strip() : "";
	}

	public String getDemoUrl() {
		return this.demoUrl;
	}

	public void setDemoUrl(String demoUrl) {
		this.demoUrl = (demoUrl != null) ? demoUrl.strip() : "";
	}

	public boolean isPublished() {
		return this.published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}

	public List<Long> getTechnologyIds() {
		return this.technologyIds;
	}

	public void setTechnologyIds(List<Long> technologyIds) {
		this.technologyIds = (technologyIds != null) ? technologyIds : new ArrayList<>();
	}

	public String getNewTechnologies() {
		return this.newTechnologies;
	}

	public void setNewTechnologies(String newTechnologies) {
		this.newTechnologies = (newTechnologies != null) ? newTechnologies : "";
	}

	public String getTitleEn() {
		return this.titleEn;
	}

	public void setTitleEn(String titleEn) {
		this.titleEn = titleEn;
	}

	public String getSummaryEn() {
		return this.summaryEn;
	}

	public void setSummaryEn(String summaryEn) {
		this.summaryEn = summaryEn;
	}

	public String getDescriptionEn() {
		return this.descriptionEn;
	}

	public void setDescriptionEn(String descriptionEn) {
		this.descriptionEn = descriptionEn;
	}

	public String getTitleDe() {
		return this.titleDe;
	}

	public void setTitleDe(String titleDe) {
		this.titleDe = titleDe;
	}

	public String getSummaryDe() {
		return this.summaryDe;
	}

	public void setSummaryDe(String summaryDe) {
		this.summaryDe = summaryDe;
	}

	public String getDescriptionDe() {
		return this.descriptionDe;
	}

	public void setDescriptionDe(String descriptionDe) {
		this.descriptionDe = descriptionDe;
	}

}
