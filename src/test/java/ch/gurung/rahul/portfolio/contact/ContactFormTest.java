package ch.gurung.rahul.portfolio.contact;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Each field reports at most one problem, so the message a visitor sees does not depend on validation order.
 */
class ContactFormTest {

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void emptyEmailIsOnlyRequired() {
		assertThat(messageTemplates(form("Anna", "", "Hallo"))).containsExactly("{contact.email.required}");
	}

	@ParameterizedTest
	@ValueSource(strings = { "not-an-address", "anna@example", "anna example@example.ch", "@example.ch" })
	void malformedEmailIsInvalid(String email) {
		assertThat(messageTemplates(form("Anna", email, "Hallo"))).containsExactly("{contact.email.invalid}");
	}

	@Test
	void validFormHasNoViolations() {
		assertThat(messageTemplates(form("Anna", "anna@example.ch", "Hallo"))).isEmpty();
	}

	private Set<String> messageTemplates(ContactForm form) {
		return this.validator.validate(form)
			.stream()
			.map(ConstraintViolation::getMessageTemplate)
			.collect(Collectors.toSet());
	}

	private static ContactForm form(String name, String email, String message) {
		ContactForm form = new ContactForm();
		form.setName(name);
		form.setEmail(email);
		form.setMessage(message);
		return form;
	}

}
