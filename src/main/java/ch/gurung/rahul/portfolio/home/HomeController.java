package ch.gurung.rahul.portfolio.home;

import java.util.Locale;

import ch.gurung.rahul.portfolio.common.Languages;
import ch.gurung.rahul.portfolio.project.ProjectService;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Landing page per language, and the redirect from {@code /} to it.
 */
@Controller
public class HomeController {

	private final ProjectService projectService;

	public HomeController(ProjectService projectService) {
		this.projectService = projectService;
	}

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
	public String index(Locale locale, Model model) {
		model.addAttribute("projects", this.projectService.publishedProjects(locale));
		model.addAttribute("skillGroups", Skills.ALL);
		return "home/index";
	}

}
