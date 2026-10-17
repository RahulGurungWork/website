package ch.gurung.rahul.portfolio.common;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import ch.gurung.rahul.portfolio.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;

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
