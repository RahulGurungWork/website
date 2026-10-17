package ch.gurung.rahul.portfolio.project;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * A project as shown on its detail page, in one language.
 */
public record ProjectDetail(String slug, String title, String summary, String description, ProjectStatus status,
		LocalDate startedOn, LocalDate endedOn, String repoUrl, String demoUrl, List<String> technologies,
		List<Image> images) {

	/**
	 * The description split into paragraphs at blank lines, so content migrations can write plain text.
	 */
	public List<String> paragraphs() {
		return Arrays.stream(this.description.split("\\R\\s*\\R"))
			.map((paragraph) -> paragraph.strip().replaceAll("\\s*\\R\\s*", " "))
			.filter((paragraph) -> !paragraph.isEmpty())
			.toList();
	}

	/**
	 * Screenshot with its alt text in the page language. {@code path} is relative to {@code /img/projects/}.
	 */
	public record Image(String path, String alt) {

	}

}
