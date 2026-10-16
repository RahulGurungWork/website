package ch.gurung.rahul.portfolio.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Landing page. Placeholder for M1; language-prefixed routes (/en, /de) arrive in M3.
 */
@Controller
public class HomeController {

	@GetMapping("/")
	public String index() {
		return "home/index";
	}

}
