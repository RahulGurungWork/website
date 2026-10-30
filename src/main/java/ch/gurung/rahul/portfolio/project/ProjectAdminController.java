package ch.gurung.rahul.portfolio.project;

import java.util.Locale;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Admin pages for projects (step A2, docs/hermes/08-admin-panel.md). Only reachable when logged in
 * ({@code SecurityConfig}). Each form posts to the URL it was shown on, so the language switcher keeps working; after a
 * change the browser is redirected (post/redirect/get) with a short notice.
 */
@Controller
public class ProjectAdminController {

	private static final String NOTICE = "notice";

	private final ProjectAdminService service;

	public ProjectAdminController(ProjectAdminService service) {
		this.service = service;
	}

	@GetMapping("/{lang:en|de}/admin/projects")
	public String list(Locale locale, Model model) {
		model.addAttribute("projects", this.service.list(locale));
		return "admin/projects/list";
	}

	@GetMapping("/{lang:en|de}/admin/projects/new")
	public String newForm(Model model) {
		return showForm(model, new ProjectForm(), null);
	}

	@PostMapping("/{lang:en|de}/admin/projects/new")
	public String create(@PathVariable String lang, @Valid @ModelAttribute("projectForm") ProjectForm form,
			BindingResult result, Model model, RedirectAttributes redirect) {
		checkRules(form, null, result);
		if (result.hasErrors()) {
			return showForm(model, form, null);
		}
		this.service.create(form);
		redirect.addFlashAttribute(NOTICE, "admin.project.created");
		return "redirect:/" + lang + "/admin/projects";
	}

	@GetMapping("/{lang:en|de}/admin/projects/{id}/edit")
	public String editForm(@PathVariable long id, Model model) {
		return showForm(model, this.service.form(id).orElseThrow(ProjectAdminController::notFound), id);
	}

	@PostMapping("/{lang:en|de}/admin/projects/{id}/edit")
	public String update(@PathVariable String lang, @PathVariable long id,
			@Valid @ModelAttribute("projectForm") ProjectForm form, BindingResult result, Model model,
			RedirectAttributes redirect) {
		checkRules(form, id, result);
		if (result.hasErrors()) {
			return showForm(model, form, id);
		}
		if (!this.service.update(id, form)) {
			throw notFound();
		}
		redirect.addFlashAttribute(NOTICE, "admin.project.saved");
		return "redirect:/" + lang + "/admin/projects";
	}

	@PostMapping("/{lang:en|de}/admin/projects/{id}/publish")
	public String publish(@PathVariable String lang, @PathVariable long id, @RequestParam boolean published,
			RedirectAttributes redirect) {
		if (!this.service.setPublished(id, published)) {
			throw notFound();
		}
		redirect.addFlashAttribute(NOTICE, published ? "admin.project.publishedNotice" : "admin.project.hiddenNotice");
		return "redirect:/" + lang + "/admin/projects";
	}

	@PostMapping("/{lang:en|de}/admin/projects/{id}/move")
	public String move(@PathVariable String lang, @PathVariable long id, @RequestParam String direction) {
		this.service.move(id, "up".equals(direction));
		return "redirect:/" + lang + "/admin/projects";
	}

	@GetMapping("/{lang:en|de}/admin/projects/{id}/delete")
	public String confirmDelete(@PathVariable long id, Locale locale, Model model) {
		model.addAttribute("projectId", id);
		model.addAttribute("projectTitle", this.service.title(id, locale).orElseThrow(ProjectAdminController::notFound));
		return "admin/projects/delete";
	}

	@PostMapping("/{lang:en|de}/admin/projects/{id}/delete")
	public String delete(@PathVariable String lang, @PathVariable long id, RedirectAttributes redirect) {
		this.service.delete(id);
		redirect.addFlashAttribute(NOTICE, "admin.project.deleted");
		return "redirect:/" + lang + "/admin/projects";
	}

	/**
	 * Rules that need the database or two fields: a unique slug, and an end date not before the start date.
	 */
	private void checkRules(ProjectForm form, Long id, BindingResult result) {
		if (!result.hasFieldErrors("slug") && this.service.slugTaken(form.getSlug(), id)) {
			result.rejectValue("slug", "admin.project.slug.taken");
		}
		if (form.getStartedOn() != null && form.getEndedOn() != null
				&& form.getEndedOn().isBefore(form.getStartedOn())) {
			result.rejectValue("endedOn", "admin.project.dates.order");
		}
	}

	private String showForm(Model model, ProjectForm form, Long id) {
		model.addAttribute("projectForm", form);
		model.addAttribute("projectId", id);
		model.addAttribute("technologies", this.service.technologies());
		model.addAttribute("statuses", ProjectStatus.values());
		model.addAttribute("fieldOrder", ProjectForm.fieldOrder());
		return "admin/projects/form";
	}

	private static ResponseStatusException notFound() {
		return new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

}
