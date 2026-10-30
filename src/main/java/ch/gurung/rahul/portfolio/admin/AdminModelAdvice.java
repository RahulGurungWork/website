package ch.gurung.rahul.portfolio.admin;

import java.security.Principal;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Adds {@code adminUser} (the logged-in admin's name, or {@code null}) to every model. The admin layout shows the
 * logout button when it is set. Public pages have no session, so it is always {@code null} there.
 */
@ControllerAdvice
public class AdminModelAdvice {

	@ModelAttribute("adminUser")
	public String adminUser(Principal principal) {
		return (principal != null) ? principal.getName() : null;
	}

}
