package ch.gurung.rahul.portfolio.cv;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import ch.gurung.rahul.portfolio.TestcontainersConfiguration;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FR10: the CV is a real PDF built from the seeded data, in the page language, without private details.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class CvIT {

	private final HttpClient client = HttpClient.newHttpClient();

	@Value("${local.server.port}")
	private int port;

	@ParameterizedTest
	@CsvSource(delimiter = '|', value = {
			"en | Informatiker EFZ Applikationsentwicklung, in training | Notion project management board | Curriculum vitae",
			"de | Informatiker EFZ Applikationsentwicklung in Ausbildung | Notion-Projektmanagement-Board | Lebenslauf" })
	void rendersCvFromSiteDataInLanguage(String lang, String role, String project, String title) throws Exception {
		HttpResponse<byte[]> response = get("/" + lang + "/cv.pdf");

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.headers().firstValue("Content-Type")).hasValue("application/pdf");
		assertThat(response.headers().firstValue("Content-Disposition"))
			.hasValueSatisfying((value) -> assertThat(value).contains("Rahul-Gurung-CV-" + lang + ".pdf"));

		try (PDDocument pdf = PDDocument.load(response.body())) {
			assertThat(pdf.getDocumentInformation().getTitle()).startsWith(title);
			assertThat(pdf.getNumberOfPages()).as("one-page CV").isEqualTo(1);
			String text = new PDFTextStripper().getText(pdf).replaceAll("\\s+", " ");
			assertThat(text).contains("Rahul Gurung", role, "rahul@gurung.ch", "gibb Berufsfachschule Bern", project,
					"n8n", "Python", "Spring Boot", "C1 Advanced (Certificate in Advanced English)", "Learn Bash Scripting",
					"18");
			assertThat(text).doesNotContain("Gfellerstrasse", "Lead Link, Kreis BI-Lernende", "Coping in a Toxic");
		}
	}

	private HttpResponse<byte[]> get(String path) throws IOException, InterruptedException {
		return this.client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + this.port + path)).build(),
				HttpResponse.BodyHandlers.ofByteArray());
	}

}
