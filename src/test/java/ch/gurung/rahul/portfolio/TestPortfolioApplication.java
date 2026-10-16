package ch.gurung.rahul.portfolio;

import org.springframework.boot.SpringApplication;

public class TestPortfolioApplication {

	public static void main(String[] args) {
		SpringApplication.from(PortfolioApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
