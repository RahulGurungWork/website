package ch.gurung.rahul.portfolio.contact;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Admin inbox for contact messages (step A4). Opening a message marks it as read, like an email program.
 */
@Controller
public class MessageAdminController {

	private static final String NOTICE = "notice";

	private final MessageAdminService service;

	public MessageAdminController(MessageAdminService service) {
		this.service = service;
	}

	@GetMapping("/{lang:en|de}/admin/messages")
	public String list(Model model) {
		model.addAttribute("messages", this.service.list());
		model.addAttribute("unread", this.service.unreadCount());
		return "admin/messages/list";
	}

	@GetMapping("/{lang:en|de}/admin/messages/{id}")
	public String show(@PathVariable long id, Model model) {
		model.addAttribute("message", this.service.open(id).orElseThrow(MessageAdminController::notFound));
		return "admin/messages/detail";
	}

	@PostMapping("/{lang:en|de}/admin/messages/{id}/unread")
	public String markUnread(@PathVariable String lang, @PathVariable long id, RedirectAttributes redirect) {
		if (!this.service.markUnread(id)) {
			throw notFound();
		}
		redirect.addFlashAttribute(NOTICE, "admin.message.markedUnread");
		return "redirect:/" + lang + "/admin/messages";
	}

	@GetMapping("/{lang:en|de}/admin/messages/{id}/delete")
	public String confirmDelete(@PathVariable long id, Model model) {
		model.addAttribute("messageId", id);
		model.addAttribute("sender", this.service.sender(id).orElseThrow(MessageAdminController::notFound));
		return "admin/messages/delete";
	}

	@PostMapping("/{lang:en|de}/admin/messages/{id}/delete")
	public String delete(@PathVariable String lang, @PathVariable long id, RedirectAttributes redirect) {
		this.service.delete(id);
		redirect.addFlashAttribute(NOTICE, "admin.message.deleted");
		return "redirect:/" + lang + "/admin/messages";
	}

	private static ResponseStatusException notFound() {
		return new ResponseStatusException(HttpStatus.NOT_FOUND);
	}

}
