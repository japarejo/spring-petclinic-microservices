package org.springframework.samples.securityoauth2microservice.restcontrollers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.securityoauth2microservice.service.InternalUserDetailsService;
import org.springframework.samples.securityoauth2microservice.util.JwtRequest;
import org.springframework.samples.securityoauth2microservice.util.JwtResponse;
import org.springframework.samples.securityoauth2microservice.util.JwtTokenRequest;
import org.springframework.samples.securityoauth2microservice.util.JwtTokenUtil;
import org.springframework.samples.securityoauth2microservice.util.JwtValidationResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@Tag(name = "Authentication", description = "Local and OAuth2 authentication endpoints")
public class JwtAuthenticationController {

	private final AuthenticationManager authenticationManager;
	private final JwtTokenUtil jwtTokenUtil;
	private final InternalUserDetailsService userDetailsService;

	public JwtAuthenticationController(
			AuthenticationManager authenticationManager,
			JwtTokenUtil jwtTokenUtil,
			InternalUserDetailsService userDetailsService) {
		this.authenticationManager = authenticationManager;
		this.jwtTokenUtil = jwtTokenUtil;
		this.userDetailsService = userDetailsService;
	}

	@Operation(summary = "Authenticate with local credentials", description = "Validates local credentials and returns a JWT token on success.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Authentication succeeded",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))),
			@ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
	})
	@RequestMapping(value = "/authenticate", method = RequestMethod.POST)
	public ResponseEntity<JwtResponse> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest)
			throws Exception {
		Authentication authentication = authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
		return ResponseEntity.ok(new JwtResponse(jwtTokenUtil.generateToken(authentication)));
	}

	@Operation(summary = "Login form", description = "HTML form that authenticates with local username and password through Spring Security form login.")
	@GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> loginForm(@RequestParam(required = false) String error) {
		String errorMessage = error != null
				? "<p class=\"error\">Invalid username or password.</p>"
				: "";
		return ResponseEntity.ok()
				.contentType(MediaType.TEXT_HTML)
				.body("""
						<!doctype html>
						<html lang="en">
						<head>
							<meta charset="utf-8">
							<meta name="viewport" content="width=device-width, initial-scale=1">
							<title>Petclinic Auth</title>
							<style>
								body { font-family: Arial, sans-serif; margin: 40px; max-width: 720px; color: #1f2937; }
								form { display: grid; gap: 12px; max-width: 360px; margin-bottom: 24px; }
								label { display: grid; gap: 6px; font-weight: 700; }
								input { padding: 9px 10px; border: 1px solid #cbd5e1; border-radius: 4px; font: inherit; }
								button, a.button { display: inline-block; width: fit-content; padding: 9px 12px; border: 0; border-radius: 4px; background: #2563eb; color: white; text-decoration: none; font: inherit; cursor: pointer; }
								.error { color: #b91c1c; font-weight: 700; }
								code { background: #f1f5f9; padding: 2px 4px; border-radius: 3px; }
							</style>
						</head>
						<body>
							<h1>Petclinic Auth</h1>
							<h2>Local user/password</h2>
							%s
							<form method="post" action="/login">
								<label>Username <input name="username" autocomplete="username" value="owner1" required></label>
								<label>Password <input name="password" type="password" autocomplete="current-password" value="0wn3r" required></label>
								<button type="submit">Sign in with form login</button>
							</form>
							<h2>OAuth2</h2>
							<p><a class="button" href="/oauth2/authorization/google">Sign in with Google</a></p>
							<p>Both flows return a JWT response with an <code>auth_provider</code> claim.</p>
						</body>
						</html>
						""".formatted(errorMessage));
	}

	@Operation(summary = "OAuth2 login entry points", description = "Start OAuth2 login with /oauth2/authorization/google, /oauth2/authorization/github or /oauth2/authorization/azure.")
	@GetMapping("/oauth2/providers")
	public String oauth2Providers() {
		return "OAuth2 login providers: /oauth2/authorization/google, /oauth2/authorization/github, /oauth2/authorization/azure";
	}

	@Operation(summary = "OAuth2 login success", description = "Receives the JWT generated after a successful OAuth2 login.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OAuth2 authentication succeeded",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class)))
	})
	@GetMapping("/oauth2/success")
	public ResponseEntity<JwtResponse> oauth2Success(@RequestParam String token) {
		return ResponseEntity.ok(new JwtResponse(token));
	}

	@Operation(summary = "Validate JWT token", description = "Validates token signature, expiration and internal user existence.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Token validation result",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtValidationResponse.class)))
	})
	@RequestMapping(value = "/validate", method = RequestMethod.POST)
	public ResponseEntity<JwtValidationResponse> validateToken(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestBody(required = false) JwtTokenRequest tokenRequest) {
		String token = resolveToken(authorizationHeader, tokenRequest);
		return ResponseEntity.ok(new JwtValidationResponse(isValidToken(token)));
	}

	@Operation(summary = "Refresh JWT token", description = "Validates an existing JWT token and returns a renewed one.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Token renewed",
					content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))),
			@ApiResponse(responseCode = "401", description = "Missing or invalid token", content = @Content)
	})
	@RequestMapping(value = "/refresh", method = RequestMethod.POST)
	public ResponseEntity<JwtResponse> refreshToken(
			@RequestHeader(value = "Authorization", required = false) String authorizationHeader,
			@RequestBody(required = false) JwtTokenRequest tokenRequest) {
		String token = resolveToken(authorizationHeader, tokenRequest);
		if (!isValidToken(token)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		UserDetails userDetails = userDetailsService.loadUserByUsername(jwtTokenUtil.getUsernameFromToken(token));
		return ResponseEntity.ok(new JwtResponse(jwtTokenUtil.generateToken(userDetails)));
	}

	private Authentication authenticate(String username, String password) throws Exception {
		try {
			return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		} catch (DisabledException e) {
			throw new Exception("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			throw new Exception("INVALID_CREDENTIALS", e);
		}
	}

	private boolean isValidToken(String token) {
		if (token == null || token.isBlank() || !jwtTokenUtil.validateTokenSignatureAndExpiration(token)) {
			return false;
		}

		try {
			UserDetails userDetails = userDetailsService.loadUserByUsername(jwtTokenUtil.getUsernameFromToken(token));
			return jwtTokenUtil.validateToken(token, userDetails);
		} catch (UsernameNotFoundException | IllegalArgumentException e) {
			return false;
		}
	}

	private String resolveToken(String authorizationHeader, JwtTokenRequest tokenRequest) {
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			return authorizationHeader.substring(7);
		}
		return tokenRequest != null ? tokenRequest.getToken() : null;
	}
}
