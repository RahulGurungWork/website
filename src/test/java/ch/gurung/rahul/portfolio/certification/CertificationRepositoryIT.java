package ch.gurung.rahul.portfolio.certification;

import ch.gurung.rahul.portfolio.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class CertificationRepositoryIT {

	@Autowired
	private CertificationRepository certifications;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void listsCertificationsBySortOrder() {
		insertCertification("Second", 20);
		insertCertification("First", 10);

		assertThat(this.certifications.findAllByOrderBySortOrderAsc()).extracting(Certification::getName)
			.containsExactly("First", "Second");
	}

	private void insertCertification(String name, int sortOrder) {
		this.entityManager.getEntityManager()
			.createNativeQuery("insert into certification (name, issuer, sort_order) values (?, 'Issuer', ?)")
			.setParameter(1, name)
			.setParameter(2, sortOrder)
			.executeUpdate();
	}

}
