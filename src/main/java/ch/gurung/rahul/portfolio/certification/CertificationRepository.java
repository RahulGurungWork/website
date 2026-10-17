package ch.gurung.rahul.portfolio.certification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificationRepository extends JpaRepository<Certification, Long> {

	List<Certification> findAllByOrderBySortOrderAsc();

}
