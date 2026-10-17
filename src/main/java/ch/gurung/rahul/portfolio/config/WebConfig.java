package ch.gurung.rahul.portfolio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC settings. Implements {@link WebMvcConfigurer} so that {@code @WebMvcTest} slices pick it up too.
 */
@Configuration(proxyBeanMethods = false)
public class WebConfig implements WebMvcConfigurer {

	/**
	 * The bean name {@code localeResolver} replaces Spring Boot's Accept-Language based resolver.
	 */
	@Bean
	LocaleResolver localeResolver() {
		return new PathLocaleResolver();
	}

}
