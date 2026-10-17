package ch.gurung.rahul.portfolio.home;

import ch.gurung.rahul.portfolio.common.Languages;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Landing page per language, and the redirect from {@code /} to it.
 */
@Controller
public class HomeController {

	/**
	 * Sends visitors to {@code /en} or {@code /de} according to their browser language; English by default.
	 */
	@GetMapping("/")
	public String root(@RequestHeader(name = HttpHeaders.ACCEPT_LANGUAGE, required = false) String acceptLanguage,
			HttpServletResponse response) {
		response.setHeader(HttpHeaders.VARY, HttpHeaders.ACCEPT_LANGUAGE);
		return "redirect:/" + Languages.fromAcceptLanguage(acceptLanguage);
	}

	@GetMapping("/{lang:en|de}")
	public String index() {
		return "home/index";
	}

}
