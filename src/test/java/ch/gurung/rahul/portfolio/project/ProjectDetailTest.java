package ch.gurung.rahul.portfolio.project;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectDetailTest {

	@Test
	void splitsDescriptionIntoParagraphsAtBlankLines() {
		ProjectDetail project = withDescription("First line\ncontinues here.\n\n  \r\nSecond paragraph.\n\n\n");

		assertThat(project.paragraphs()).containsExactly("First line continues here.", "Second paragraph.");
	}

	@Test
	void singleLineDescriptionIsOneParagraph() {
		assertThat(withDescription("Just one.").paragraphs()).containsExactly("Just one.");
	}

	private static ProjectDetail withDescription(String description) {
		return new ProjectDetail("slug", "Title", "Summary", description, ProjectStatus.CURRENT, null, null, null, null,
				List.of(), List.of());
	}

}
