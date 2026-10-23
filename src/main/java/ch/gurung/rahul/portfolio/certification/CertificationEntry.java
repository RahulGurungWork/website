package ch.gurung.rahul.portfolio.certification;

import java.time.LocalDate;
import java.util.List;

/**
 * A certification as shown on the certificates page. {@code issuedOn} and {@code credentialUrl} may be null.
 */
public record CertificationEntry(String name, String issuer, LocalDate issuedOn, String credentialUrl,
		CertificationCategory category) {

	/**
	 * One section of the page: a category and its certifications in sort order.
	 */
	public record Group(CertificationCategory category, List<CertificationEntry> entries) {

	}

}
