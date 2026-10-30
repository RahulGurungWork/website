package ch.gurung.rahul.portfolio.project;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

	/**
	 * All projects, published or not, for the admin list.
	 */
	@EntityGraph(attributePaths = { "translations" })
	List<Project> findAllByOrderBySortOrderAscIdAsc();

	/**
	 * One project with texts and technologies loaded, for the admin form.
	 */
	@EntityGraph(attributePaths = { "translations", "technologies" })
	Optional<Project> findWithDetailsById(Long id);

	boolean existsBySlug(String slug);

	boolean existsBySlugAndIdNot(String slug, Long id);

	@Query("select coalesce(max(p.sortOrder), 0) from Project p")
	int maxSortOrder();

}
