package ch.gurung.rahul.portfolio.certification;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Certifications for the certificates page. Names and issuers are proper names, so they are not translated.
 */
@Service
@Transactional(readOnly = true)
public class CertificationService {

	private final CertificationRepository certifications;

	public CertificationService(CertificationRepository certifications) {
		this.certifications = certifications;
	}

	public List<CertificationEntry> certifications() {
		return this.certifications.findAllByOrderBySortOrderAsc()
			.stream()
			.map((c) -> new CertificationEntry(c.getName(), c.getIssuer(), c.getIssuedOn(), c.getCredentialUrl(),
					c.getCategory()))
			.toList();
	}

	/**
	 * Certifications grouped by category, in the order of {@link CertificationCategory}; empty groups are left out.
	 */
	public List<CertificationEntry.Group> groupedCertifications() {
		List<CertificationEntry> all = certifications();
		return Arrays.stream(CertificationCategory.values())
			.map((category) -> new CertificationEntry.Group(category,
					all.stream().filter((entry) -> entry.category() == category).toList()))
			.filter((group) -> !group.entries().isEmpty())
			.toList();
	}

}
