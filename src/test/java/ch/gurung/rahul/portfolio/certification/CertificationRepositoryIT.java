package ch.gurung.rahul.portfolio.certification;

import java.util.Comparator;

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
		insertCertification("Inserted last", 9999);
		insertCertification("Inserted first", -1);

		assertThat(this.certifications.findAllByOrderBySortOrderAsc())
			.isSortedAccordingTo(Comparator.comparingInt(Certification::getSortOrder))
			.first()
			.extracting(Certification::getName)
			.isEqualTo("Inserted first");
		assertThat(this.certifications.findAllByOrderBySortOrderAsc()).last()
			.extracting(Certification::getName)
			.isEqualTo("Inserted last");
	}

	private void insertCertification(String name, int sortOrder) {
		this.entityManager.getEntityManager()
			.createNativeQuery("insert into certification (name, issuer, sort_order) values (?, 'Issuer', ?)")
			.setParameter(1, name)
			.setParameter(2, sortOrder)
			.executeUpdate();
	}

}
