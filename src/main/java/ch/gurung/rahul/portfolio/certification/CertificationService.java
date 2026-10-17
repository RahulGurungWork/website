package ch.gurung.rahul.portfolio.certification;

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
			.map((c) -> new CertificationEntry(c.getName(), c.getIssuer(), c.getIssuedOn(), c.getCredentialUrl()))
			.toList();
	}

}
