package ch.gurung.rahul.portfolio.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Admin login page and dashboard. The login form posts to {@code /admin/login}, which Spring Security handles
 * ({@code SecurityConfig}).
 */
@Controller
public class AdminController {

	private final AdminProperties properties;

	public AdminController(AdminProperties properties) {
		this.properties = properties;
	}

	@GetMapping("/{lang:en|de}/admin/login")
	public String login(Model model) {
		model.addAttribute("adminConfigured", this.properties.configured());
		return "admin/login";
	}

	/**
	 * The model's {@code adminUser} comes from {@link AdminModelAdvice}.
	 */
	@GetMapping("/{lang:en|de}/admin")
	public String dashboard() {
		return "admin/dashboard";
	}

}
