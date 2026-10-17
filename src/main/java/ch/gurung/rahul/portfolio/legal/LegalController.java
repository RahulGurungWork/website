package ch.gurung.rahul.portfolio.legal;

import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Legal notice (Impressum) and privacy notice. The texts are long and legally worded, so each language has its own
 * template ({@code legal/privacy-en.html}, {@code legal/privacy-de.html}) instead of message keys.
 */
@Controller
public class LegalController {

	@GetMapping("/{lang:en|de}/legal-notice")
	public String legalNotice(Locale locale) {
		return "legal/legal-notice-" + locale.getLanguage();
	}

	@GetMapping("/{lang:en|de}/privacy")
	public String privacy(Locale locale) {
		return "legal/privacy-" + locale.getLanguage();
	}

}
