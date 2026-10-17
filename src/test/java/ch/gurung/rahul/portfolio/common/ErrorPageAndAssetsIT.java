package ch.gurung.rahul.portfolio.common;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import ch.gurung.rahul.portfolio.TestcontainersConfiguration;
import ch.gurung.rahul.portfolio.contact.ContactMessage;
import ch.gurung.rahul.portfolio.contact.ContactMessageRepository;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Runs the real server, because error pages and static resources are not covered by MockMvc.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class ErrorPageAndAssetsIT {

	private final HttpClient client = HttpClient.newHttpClient();

	@Value("${local.server.port}")
	private int port;

	@Autowired
	private ContactMessageRepository contactMessages;

	@Test
	void unknownPageShowsStyledNotFoundInVisitorsLanguage() throws Exception {
		HttpResponse<String> german = get("/de/does-not-exist");
		assertThat(german.statusCode()).isEqualTo(404);
		assertThat(german.body()).contains("<html lang=\"de\"", "Seite nicht gefunden", "href=\"/de\"");

		HttpResponse<String> english = get("/en/does-not-exist");
		assertThat(english.statusCode()).isEqualTo(404);
		assertThat(english.body()).contains("<html lang=\"en\"", "Page not found");
	}

	@Test
	void unknownProjectShowsStyledNotFound() throws Exception {
		HttpResponse<String> response = get("/de/projects/does-not-exist");

		assertThat(response.statusCode()).isEqualTo(404);
		assertThat(response.body()).contains("Seite nicht gefunden");
	}

	@Test
	void seededProjectPagesRenderEndToEnd() throws Exception {
		HttpResponse<String> list = get("/en/projects");
		assertThat(list.statusCode()).isEqualTo(200);
		assertThat(list.body()).contains("Notion workspace prototype", "n8n workflow", "Learning documentation generator");

		HttpResponse<String> detail = get("/de/projects/lerndoku-generator");
		assertThat(detail.statusCode()).isEqualTo(200);
		assertThat(detail.body()).contains("<h1>Lerndokumentations-Generator</h1>", "Ausführliche Beschreibung folgt.");
	}

	@Test
	void certificateAndLegalPagesRenderEndToEnd() throws Exception {
		assertThat(get("/en/certificates").body()).contains("<h1>Certificates</h1>",
				"C1 Advanced (Certificate in Advanced English)", "<time datetime=\"2026-07\">07.2026</time>");
		assertThat(get("/de/legal-notice").statusCode()).isEqualTo(200);
		assertThat(get("/de/privacy").body()).contains("Datenschutzerklärung");
	}

	/**
	 * No SMTP server runs here, so the mail usually fails; either way the message must be stored.
	 */
	@Test
	void contactFormStoresMessageAndRedirectsToConfirmation() throws Exception {
		String marker = "End-to-end test " + System.nanoTime();
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + this.port + "/de/contact"))
			.header("Content-Type", "application/x-www-form-urlencoded")
			.POST(HttpRequest.BodyPublishers.ofString("name=Anna+Muster&email=anna%40example.ch&message="
					+ URLEncoder.encode(marker, StandardCharsets.UTF_8) + "&website="))
			.build();

		HttpResponse<String> response = this.client.send(request, HttpResponse.BodyHandlers.ofString());

		assertThat(response.statusCode()).isEqualTo(302);
		assertThat(response.headers().firstValue("Location")).hasValueSatisfying(
				(location) -> assertThat(location).endsWith("/de/contact/sent"));
		assertThat(this.contactMessages.findAll()).extracting(ContactMessage::getMessage).contains(marker);
		assertThat(get("/de/contact/sent").body()).contains("Vielen Dank!");
	}

	/**
	 * No SMTP server runs here; health must still be UP (CI's Lighthouse job waits for it).
	 */
	@Test
	void healthIsUpWithoutMailServer() throws Exception {
		HttpResponse<String> health = this.client.send(
				HttpRequest.newBuilder(URI.create("http://localhost:" + this.port + "/actuator/health")).build(),
				HttpResponse.BodyHandlers.ofString());

		assertThat(health.statusCode()).isEqualTo(200);
		assertThat(health.body()).contains("\"status\":\"UP\"").doesNotContain("\"mail\"");
	}

	@Test
	void servesStylesheets() throws Exception {
		assertThat(get("/webjars/bootstrap/dist/css/bootstrap.min.css").statusCode()).isEqualTo(200);
		assertThat(get("/css/site.css").statusCode()).isEqualTo(200);
	}

	private HttpResponse<String> get(String path) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + this.port + path))
			.header("Accept", "text/html")
			.build();
		return this.client.send(request, HttpResponse.BodyHandlers.ofString());
	}

}
