package ch.gurung.rahul.portfolio.certification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CertificationRepository extends JpaRepository<Certification, Long> {

	List<Certification> findAllByOrderBySortOrderAsc();

	/**
	 * All certifications in page order, ties broken by id so the admin list is stable.
	 */
	List<Certification> findAllByOrderBySortOrderAscIdAsc();

	@Query("select coalesce(max(c.sortOrder), 0) from Certification c")
	int maxSortOrder();

}
