package ch.gurung.rahul.portfolio.common;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Model attributes every page layout needs (see {@code templates/layout/base.html}).
 * <ul>
 * <li>{@code lang}: current language, {@code en} or {@code de}</li>
 * <li>{@code page}: current path without language, for example {@code ""} or {@code /projects}</li>
 * <li>{@code languageLinks}: language to the same page in that language, for the switcher</li>
 * <li>{@code alternateUrls}: language to the absolute URL of that page, for {@code hreflang} links</li>
 * </ul>
 */
@ControllerAdvice
public class LayoutModelAdvice {

	@ModelAttribute
	public void addLayoutAttributes(HttpServletRequest request, Model model) {
		String path = Languages.requestPath(request);
		String page = Languages.withoutLanguage(path);

		Map<String, String> languageLinks = new LinkedHashMap<>();
		Map<String, String> alternateUrls = new LinkedHashMap<>();
		for (String language : Languages.SUPPORTED) {
			String link = "/" + language + page;
			languageLinks.put(language, link);
			alternateUrls.put(language, ServletUriComponentsBuilder.fromContextPath(request).path(link).toUriString());
		}

		model.addAttribute("lang", Languages.fromPath(path).orElse(Languages.DEFAULT));
		model.addAttribute("page", page);
		model.addAttribute("languageLinks", languageLinks);
		model.addAttribute("alternateUrls", alternateUrls);
	}

}
