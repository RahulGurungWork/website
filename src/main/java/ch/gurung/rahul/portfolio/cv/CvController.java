package ch.gurung.rahul.portfolio.cv;

import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * CV download (FR10), generated on request in the page language.
 */
@Controller
public class CvController {

	private final CvService cvService;

	public CvController(CvService cvService) {
		this.cvService = cvService;
	}

	@GetMapping("/{lang:en|de}/cv.pdf")
	public ResponseEntity<byte[]> cv(Locale locale, HttpServletRequest request) {
		String siteUrl = ServletUriComponentsBuilder.fromContextPath(request).toUriString();
		byte[] pdf = this.cvService.render(locale, siteUrl);
		String filename = "Rahul-Gurung-CV-" + locale.getLanguage() + ".pdf";
		return ResponseEntity.ok()
			.contentType(MediaType.APPLICATION_PDF)
			.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(filename).build().toString())
			.body(pdf);
	}

}
