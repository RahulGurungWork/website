package ch.gurung.rahul.portfolio.project;

import java.util.Locale;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

/**
 * Screenshot of a {@link Project}. {@code path} is relative to {@code static/img/projects/}.
 */
@Entity
public class ProjectImage {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	private Project project;

	private String path;

	private String altEn;

	private String altDe;

	private int sortOrder;

	protected ProjectImage() {
	}

	public Long getId() {
		return this.id;
	}

	public Project getProject() {
		return this.project;
	}

	public String getPath() {
		return this.path;
	}

	public String getAltEn() {
		return this.altEn;
	}

	public String getAltDe() {
		return this.altDe;
	}

	public int getSortOrder() {
		return this.sortOrder;
	}

	/**
	 * Alt text for the given locale; German for {@code de}, English otherwise.
	 */
	public String alt(Locale locale) {
		return "de".equals(locale.getLanguage()) ? this.altDe : this.altEn;
	}

}
