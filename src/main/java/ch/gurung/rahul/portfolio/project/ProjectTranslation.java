package ch.gurung.rahul.portfolio.project;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Title and texts of a {@link Project} in one language ({@code en} or {@code de}).
 */
@Entity
public class ProjectTranslation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	private Project project;

	private String locale;

	private String title;

	private String summary;

	private String description;

	protected ProjectTranslation() {
	}

	public Long getId() {
		return this.id;
	}

	public Project getProject() {
		return this.project;
	}

	public String getLocale() {
		return this.locale;
	}

	public String getTitle() {
		return this.title;
	}

	public String getSummary() {
		return this.summary;
	}

	public String getDescription() {
		return this.description;
	}

}
