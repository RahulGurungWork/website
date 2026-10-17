package ch.gurung.rahul.portfolio.project;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

/**
 * Project list and detail pages. The locale comes from the URL (see PathLocaleResolver).
 */
@Controller
public class ProjectController {

	private final ProjectService projectService;

	public ProjectController(ProjectService projectService) {
		this.projectService = projectService;
	}

	@GetMapping("/{lang:en|de}/projects")
	public String list(Locale locale, Model model) {
		model.addAttribute("projects", this.projectService.publishedProjects(locale));
		return "project/list";
	}

	/**
	 * Unknown or unpublished slugs answer 404, which renders the styled error page.
	 */
	@GetMapping("/{lang:en|de}/projects/{slug}")
	public String detail(@PathVariable String slug, Locale locale, Model model) {
		ProjectDetail project = this.projectService.publishedProject(slug, locale)
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		model.addAttribute("project", project);
		return "project/detail";
	}

}
