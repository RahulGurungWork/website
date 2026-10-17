package ch.gurung.rahul.portfolio.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Application-wide beans. Scheduling runs the nightly contact-message cleanup.
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
public class AppConfig {

	/**
	 * Injected wherever time matters (rate limit, cleanup), so tests can use a fixed clock.
	 */
	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}

}
