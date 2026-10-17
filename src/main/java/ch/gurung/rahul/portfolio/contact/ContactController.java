package ch.gurung.rahul.portfolio.contact;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Contact form (flow: docs/diagrams/flow-contact.puml). Spam check first, then validation, then store and mail,
 * then redirect to the confirmation page (post/redirect/get).
 */
@Controller
public class ContactController {

	private final SpamGuard spamGuard;

	private final ContactService contactService;

	public ContactController(SpamGuard spamGuard, ContactService contactService) {
		this.spamGuard = spamGuard;
		this.contactService = contactService;
	}

	@GetMapping("/{lang:en|de}/contact")
	public String form(Model model) {
		model.addAttribute("contactForm", new ContactForm());
		return "contact/form";
	}

	/**
	 * The client IP comes from {@code getRemoteAddr()}; behind Caddy, {@code server.forward-headers-strategy} makes
	 * that the visitor's address.
	 */
	@PostMapping("/{lang:en|de}/contact")
	public String submit(@PathVariable String lang, @Valid @ModelAttribute ContactForm contactForm,
			BindingResult result, HttpServletRequest request) {
		if (this.spamGuard.isSpam(contactForm, request.getRemoteAddr())) {
			return "redirect:/" + lang + "/contact/sent";
		}
		if (result.hasErrors()) {
			return "contact/form";
		}
		this.contactService.submit(contactForm);
		return "redirect:/" + lang + "/contact/sent";
	}

	@GetMapping("/{lang:en|de}/contact/sent")
	public String sent() {
		return "contact/sent";
	}

}
