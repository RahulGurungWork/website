package ch.gurung.rahul.portfolio.technology;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TechnologyRepository extends JpaRepository<Technology, Long> {

	Optional<Technology> findByName(String name);

	Optional<Technology> findByNameIgnoreCase(String name);

}
