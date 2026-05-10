package org.springframework.samples.securityoauth2microservice.util;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.samples.securityoauth2microservice.model.InternalUser;
import org.springframework.samples.securityoauth2microservice.service.ExternalIdentityService;
import org.springframework.samples.securityoauth2microservice.service.InternalUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2JwtSuccessHandler implements AuthenticationSuccessHandler {

	private final ExternalIdentityService externalIdentityService;
	private final InternalUserDetailsService userDetailsService;
	private final JwtTokenUtil jwtTokenUtil;
	private final String successRedirectUri;

	public OAuth2JwtSuccessHandler(
			ExternalIdentityService externalIdentityService,
			InternalUserDetailsService userDetailsService,
			JwtTokenUtil jwtTokenUtil,
			@Value("${app.oauth2.success-redirect-uri}") String successRedirectUri) {
		this.externalIdentityService = externalIdentityService;
		this.userDetailsService = userDetailsService;
		this.jwtTokenUtil = jwtTokenUtil;
		this.successRedirectUri = successRedirectUri;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			org.springframework.security.core.Authentication authentication) throws IOException, ServletException {
		OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
		InternalUser internalUser = externalIdentityService.findOrCreateUser(
				oauthToken.getAuthorizedClientRegistrationId(),
				oauthToken.getPrincipal());
		UserDetails userDetails = userDetailsService.toUserDetails(internalUser);
		String jwt = jwtTokenUtil.generateToken(userDetails);
		response.sendRedirect(successRedirectUri + "?token=" + URLEncoder.encode(jwt, StandardCharsets.UTF_8));
	}
}
