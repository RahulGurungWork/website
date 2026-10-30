package ch.gurung.rahul.portfolio.certification;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

	@Enumerated(EnumType.STRING)
	private CertificationCategory category;

	protected Certification() {
	}

	/**
	 * A new certification at {@code sortOrder}; the admin fills in the rest with {@link #update}.
	 */
	public Certification(int sortOrder) {
		this.sortOrder = sortOrder;
		this.category = CertificationCategory.PROFESSIONAL;
	}

	/**
	 * Sets everything except the order (admin panel, step A3).
	 */
	void update(String name, String issuer, LocalDate issuedOn, String credentialUrl, CertificationCategory category) {
		this.name = name;
		this.issuer = issuer;
		this.issuedOn = issuedOn;
		this.credentialUrl = credentialUrl;
		this.category = category;
	}

	void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
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

	public CertificationCategory getCategory() {
		return this.category;
	}

}
