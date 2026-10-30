package ch.gurung.rahul.portfolio.technology;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/**
 * A technology shown on project pages, such as "Spring Boot" or "n8n".
 */
@Entity
public class Technology {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	protected Technology() {
	}

	/**
	 * A technology the admin typed in that did not exist yet.
	 */
	public Technology(String name) {
		this.name = name;
	}

	public Long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

}
