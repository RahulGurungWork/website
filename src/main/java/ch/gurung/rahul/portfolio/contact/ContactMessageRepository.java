package ch.gurung.rahul.portfolio.contact;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

	/**
	 * Messages whose notification mail failed, oldest first, for a later retry.
	 */
	List<ContactMessage> findByMailSentFalseOrderByCreatedAtAsc();

	/**
	 * Deletes messages created before {@code cutoff} and returns how many were deleted.
	 */
	@Modifying
	@Transactional
	@Query("delete from ContactMessage m where m.createdAt < :cutoff")
	int deleteCreatedBefore(@Param("cutoff") Instant cutoff);

}
