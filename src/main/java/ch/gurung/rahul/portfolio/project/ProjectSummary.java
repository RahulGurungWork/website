package ch.gurung.rahul.portfolio.project;

import java.util.List;

/**
 * A project as shown on a card: list page and home page. {@code cover} is the first screenshot, or {@code null}
 * while the project has none.
 */
public record ProjectSummary(String slug, String title, String summary, ProjectStatus status,
		List<String> technologies, ProjectDetail.Image cover) {

}
