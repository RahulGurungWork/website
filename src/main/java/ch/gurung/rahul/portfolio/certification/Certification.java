package ch.gurung.rahul.portfolio.certification;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * A certification shown on the certifications page.
 */
@Entity
public class Certification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	private String issuer;

	private LocalDate issuedOn;

	private String credentialUrl;

	private int sortOrder;

	protected Certification() {
	}

	public Long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getIssuer() {
		return this.issuer;
	}

	public LocalDate getIssuedOn() {
		return this.issuedOn;
	}

	public String getCredentialUrl() {
		return this.credentialUrl;
	}

	public int getSortOrder() {
		return this.sortOrder;
	}

}
