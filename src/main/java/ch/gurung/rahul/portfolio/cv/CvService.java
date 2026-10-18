package ch.gurung.rahul.portfolio.cv;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Locale;

import ch.gurung.rahul.portfolio.certification.CertificationService;
import ch.gurung.rahul.portfolio.home.Skills;
import ch.gurung.rahul.portfolio.project.ProjectDetail;
import ch.gurung.rahul.portfolio.project.ProjectService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import org.springframework.stereotype.Service;

/**
 * Renders the CV as PDF from the same data as the website, so it never drifts from it. The template
 * ({@code templates/cv/cv.html}) is turned into HTML by Thymeleaf, into a DOM by jsoup and into a PDF by
 * OpenHTMLToPDF. It deliberately leaves out photo, date of birth, nationality and phone number.
 */
@Service
public class CvService {

	private final ITemplateEngine templateEngine;

	private final ProjectService projectService;

	private final CertificationService certificationService;

	public CvService(ITemplateEngine templateEngine, ProjectService projectService,
			CertificationService certificationService) {
		this.templateEngine = templateEngine;
		this.projectService = projectService;
		this.certificationService = certificationService;
	}

	/**
	 * @param siteUrl absolute URL of the website, printed in the CV and used to resolve relative links
	 */
	public byte[] render(Locale locale, String siteUrl) {
		Context context = new Context(locale);
		context.setVariable("lang", locale.getLanguage());
		context.setVariable("siteUrl", siteUrl);
		context.setVariable("projects", projects(locale));
		context.setVariable("skillGroups", Skills.ALL);
		context.setVariable("certificationGroups", this.certificationService.groupedCertifications());
		String html = this.templateEngine.process("cv/cv", context);

		ByteArrayOutputStream pdf = new ByteArrayOutputStream();
		try {
			PdfRendererBuilder builder = new PdfRendererBuilder();
			builder.useFastMode();
			builder.withW3cDocument(new W3CDom().fromJsoup(Jsoup.parse(html)), siteUrl);
			builder.toStream(pdf);
			builder.run();
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Rendering the CV failed", ex);
		}
		return pdf.toByteArray();
	}

	private List<ProjectDetail> projects(Locale locale) {
		return this.projectService.publishedProjects(locale)
			.stream()
			.flatMap((summary) -> this.projectService.publishedProject(summary.slug(), locale).stream())
			.toList();
	}

}
