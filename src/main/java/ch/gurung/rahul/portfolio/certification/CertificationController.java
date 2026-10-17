package ch.gurung.rahul.portfolio.certification;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CertificationController {

	private final CertificationService certificationService;

	public CertificationController(CertificationService certificationService) {
		this.certificationService = certificationService;
	}

	@GetMapping("/{lang:en|de}/certificates")
	public String list(Model model) {
		model.addAttribute("certifications", this.certificationService.certifications());
		return "certification/list";
	}

}
