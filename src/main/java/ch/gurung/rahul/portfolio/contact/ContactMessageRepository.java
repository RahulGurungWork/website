package ch.gurung.rahul.portfolio.contact;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

	/**
	 * Messages whose notification mail failed, oldest first, for a later retry.
	 */
	List<ContactMessage> findByMailSentFalseOrderByCreatedAtAsc();

}
