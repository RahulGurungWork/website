package ch.gurung.rahul.portfolio.admin;

import jakarta.servlet.http.HttpServletRequest;

/**
 * What the login form sends besides username and password: the authenticator code, plus the client IP for the
 * lockout. Behind Caddy, {@code server.forward-headers-strategy} makes {@code getRemoteAddr()} the visitor's address.
 *
 * @param remoteAddress client IP address
 * @param code 6-digit code from the authenticator app, as typed
 */
public record AdminLoginDetails(String remoteAddress, String code) {

	public static AdminLoginDetails from(HttpServletRequest request) {
		String code = request.getParameter("code");
		return new AdminLoginDetails(request.getRemoteAddr(), (code != null) ? code.replace(" ", "") : null);
	}

}
