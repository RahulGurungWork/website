package ch.gurung.rahul.portfolio.admin;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminSetupTest {

	@Test
	void linkFollowsKeyUriFormat() {
		assertThat(AdminSetup.otpauthLink("rahul", "GEZDGNBVGY3TQOJQ")).isEqualTo(
				"otpauth://totp/P008%20Portfolio%3Arahul?secret=GEZDGNBVGY3TQOJQ&issuer=P008%20Portfolio&digits=6&period=30");
	}

}
