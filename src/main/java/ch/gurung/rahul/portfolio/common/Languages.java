package ch.gurung.rahul.portfolio.common;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Site languages and the {@code /<lang>/...} URL scheme. The first path segment selects the language.
 */
public final class Languages {

	public static final List<String> SUPPORTED = List.of("en", "de");

	public static final String DEFAULT = "en";

	private static final List<Locale> SUPPORTED_LOCALES = SUPPORTED.stream().map(Locale::of).toList();

	private Languages() {
	}

	/**
	 * Path the visitor asked for, without context path. On an error dispatch this is the original path, not
	 * {@code /error}, so error pages keep the visitor's language.
	 */
	public static String requestPath(HttpServletRequest request) {
		Object errorUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
		String uri = (errorUri instanceof String s) ? s : request.getRequestURI();
		String contextPath = request.getContextPath();
		return (!contextPath.isEmpty() && uri.startsWith(contextPath)) ? uri.substring(contextPath.length()) : uri;
	}

	/**
	 * Language of a path such as {@code /de/projects}, if its first segment is a supported language.
	 */
	public static Optional<String> fromPath(String path) {
		String first = firstSegment(path);
		return SUPPORTED.contains(first) ? Optional.of(first) : Optional.empty();
	}

	/**
	 * The path without its language segment: {@code /de/projects} becomes {@code /projects}, {@code /de} becomes
	 * {@code ""}. Paths without a language segment are returned as they are.
	 */
	public static String withoutLanguage(String path) {
		return fromPath(path).map((language) -> path.substring(language.length() + 1)).orElse(path);
	}

	/**
	 * Best supported language for an {@code Accept-Language} header; {@link #DEFAULT} if none matches or the header
	 * is missing or malformed.
	 */
	public static String fromAcceptLanguage(String header) {
		if (header == null || header.isBlank()) {
			return DEFAULT;
		}
		try {
			Locale best = Locale.lookup(Locale.LanguageRange.parse(header), SUPPORTED_LOCALES);
			return (best != null) ? best.getLanguage() : DEFAULT;
		}
		catch (IllegalArgumentException ex) {
			return DEFAULT;
		}
	}

	private static String firstSegment(String path) {
		if (path == null || !path.startsWith("/")) {
			return "";
		}
		int end = path.indexOf('/', 1);
		return (end < 0) ? path.substring(1) : path.substring(1, end);
	}

}
