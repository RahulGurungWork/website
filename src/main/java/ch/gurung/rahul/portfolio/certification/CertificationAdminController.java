package ch.gurung.rahul.portfolio.certification;

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
 * Admin pages for certifications (step A3), built like the project pages: forms post to the URL they were shown on,
 * changes redirect to the list with a notice, delete asks first.
 */
@Controller
public class CertificationAdminController {

	private static final String NOTICE = "notice";

	private final CertificationAdminService service;

	public CertificationAdminController(CertificationAdminService service) {
		this.service = service;
	}

	@GetMapping("/{lang:en|de}/admin/certificates")
	public String list(Model model) {
		model.addAttribute("groups", this.service.list());
		return "admin/certificates/list";
	}

	@GetMapping("/{lang:en|de}/admin/certificates/new")
	public String newForm(Model model) {
		return showForm(model, new CertificationForm(), null);
	}

	@PostMapping("/{lang:en|de}/admin/certificates/new")
	public String create(@PathVariable String lang,
			@Valid @ModelAttribute("certificationForm") CertificationForm form, BindingResult result, Model model,
			RedirectAttributes redirect) {
		if (result.hasErrors()) {
			return showForm(model, form, null);
		}
		this.service.create(form);
		redirect.addFlashAttribute(NOTICE, "admin.certificate.created");
		return "redirect:/" + lang + "/admin/certificates";
	}

	@GetMapping("/{lang:en|de}/admin/certificates/{id}/edit")
	public String editForm(@PathVariable long id, Model model) {
		return showForm(model, this.service.form(id).orElseThrow(CertificationAdminController::notFound), id);
	}

	@PostMapping("/{lang:en|de}/admin/certificates/{id}/edit")
	public String update(@PathVariable String lang, @PathVariable long id,
			@Valid @ModelAttribute("certificationForm") CertificationForm form, BindingResult result, Model model,
			RedirectAttributes redirect) {
		if (result.hasErrors()) {
			return showForm(model, form, id);
		}
		if (!this.service.update(id, form)) {
			throw notFound();
		}
		redirect.addFlashAttribute(NOTICE, "admin.certificate.saved");
		return "redirect:/" + lang + "/admin/certificates";
	}

	@PostMapping("/{lang:en|de}/admin/certificates/{id}/move")
	public String move(@PathVariable String lang, @PathVariable long id, @RequestParam String direction) {
		this.service.move(id, "up".equals(direction));
		return "redirect:/" + lang + "/admin/certificates";
	}

	@GetMapping("/{lang:en|de}/admin/certificates/{id}/delete")
	public String confirmDelete(@PathVariable long id, Model model) {
		model.addAttribute("certificationId", id);
		model.addAttribute("certificationName",
				this.service.name(id).orElseThrow(CertificationAdminController::notFound));
		return "admin/certificates/delete";
	}

	@PostMapping("/{lang:en|de}/admin/certificates/{id}/delete")
	public String delete(@PathVariable String lang, @PathVariable long id, RedirectAttributes redirect) {
		this.service.delete(id);
		redirect.addFlashAttribute(NOTICE, "admin.certificate.deleted");
		return "redirect:/" + lang + "/admin/certificates";
	}

	private String showForm(Model model, CertificationForm form, Long id) {
		model.addAttribute("certificationForm", form);
		model.addAttribute("certificationId", id);
		model.addAttribute("categories", CertificationCategory.values());
		model.addAttribute("fieldOrder", CertificationForm.fieldOrder());
		return "admin/certificates/form";
	}

	private static ResponseStatusException notFound() {
		return new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

}
