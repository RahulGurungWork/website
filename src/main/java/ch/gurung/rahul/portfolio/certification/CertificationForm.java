package ch.gurung.rahul.portfolio.certification;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

/**
 * What the admin certification form posts (step A3). Names and issuers are proper names, so there is one value for
 * both languages. Messages in braces are keys in {@code i18n/messages*.properties}.
 */
public class CertificationForm {

	@NotBlank(message = "{admin.form.required}")
	@Size(max = 200, message = "{admin.certificate.name.tooLong}")
	private String name = "";

	@NotBlank(message = "{admin.form.required}")
	@Size(max = 200, message = "{admin.certificate.issuer.tooLong}")
	private String issuer = "";

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate issuedOn;

	@Size(max = 500, message = "{admin.project.url.tooLong}")
	@Pattern(regexp = "|https?://\\S+", message = "{admin.project.url.invalid}")
	private String credentialUrl = "";

	@NotNull(message = "{admin.form.required}")
	private CertificationCategory category = CertificationCategory.PROFESSIONAL;

	/**
	 * The fields in the order the form shows them, for the error summary.
	 */
	public static List<String> fieldOrder() {
		return List.of("name", "issuer", "issuedOn", "credentialUrl", "category");
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = (name != null) ? name.strip() : "";
	}

	public String getIssuer() {
		return this.issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = (issuer != null) ? issuer.strip() : "";
	}

	public LocalDate getIssuedOn() {
		return this.issuedOn;
	}

	public void setIssuedOn(LocalDate issuedOn) {
		this.issuedOn = issuedOn;
	}

	public String getCredentialUrl() {
		return this.credentialUrl;
	}

	public void setCredentialUrl(String credentialUrl) {
		this.credentialUrl = (credentialUrl != null) ? credentialUrl.strip() : "";
	}

	public CertificationCategory getCategory() {
		return this.category;
	}

	public void setCategory(CertificationCategory category) {
		this.category = category;
	}

}
