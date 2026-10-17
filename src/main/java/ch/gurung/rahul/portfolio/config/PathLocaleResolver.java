package ch.gurung.rahul.portfolio.config;

import java.util.Locale;

import ch.gurung.rahul.portfolio.common.Languages;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.LocaleResolver;

/**
 * Takes the locale from the URL ({@code /en/...}, {@code /de/...}); anything else is English. The language is
 * changed by following a link to the other URL, never by storing it, so {@link #setLocale} is unsupported.
 */
public class PathLocaleResolver implements LocaleResolver {

	@Override
	public Locale resolveLocale(HttpServletRequest request) {
		return Locale.of(Languages.fromPath(Languages.requestPath(request)).orElse(Languages.DEFAULT));
	}

	@Override
	public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
		throw new UnsupportedOperationException("The language comes from the URL");
	}

}
