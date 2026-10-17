package ch.gurung.rahul.portfolio.project;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

	/**
	 * Published projects for the list page, with what a card shows loaded.
	 */
	@EntityGraph(attributePaths = { "translations", "images", "technologies" })
	List<Project> findByPublishedTrueOrderBySortOrderAsc();

	/**
	 * One published project for its detail page, with everything the page shows loaded.
	 */
	@EntityGraph(attributePaths = { "translations", "images", "technologies" })
	Optional<Project> findBySlugAndPublishedTrue(String slug);

}
