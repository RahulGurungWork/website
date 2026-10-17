package ch.gurung.rahul.portfolio.certification;

import java.time.LocalDate;

/**
 * A certification as shown on the certificates page. {@code issuedOn} and {@code credentialUrl} may be null.
 */
public record CertificationEntry(String name, String issuer, LocalDate issuedOn, String credentialUrl) {

}
