package org.springframework.samples.securitymicroservice.restcontrollers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.securitymicroservice.service.JwtUserDetailsService;
import org.springframework.samples.securitymicroservice.util.JwtRequest;
import org.springframework.samples.securitymicroservice.util.JwtResponse;
import org.springframework.samples.securitymicroservice.util.JwtTokenRequest;
import org.springframework.samples.securitymicroservice.util.JwtTokenUtil;
import org.springframework.samples.securitymicroservice.util.JwtValidationResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@Tag(name = "Authentication", description = "JWT authentication endpoints")
public class JwtAuthenticationController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private JwtUserDetailsService userDetailsService;

	@Operation(summary = "Authenticate user", description = "Validates credentials and returns a JWT token on success.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Authentication succeeded",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))),
		@ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content)
	})
	@RequestMapping(value = "/authenticate", method = RequestMethod.POST)
	public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {

		Authentication authentication=authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());		

		final String token = jwtTokenUtil.generateToken(authentication);
		return ResponseEntity.ok(new JwtResponse(token));

	}

	@Operation(summary = "Validate JWT token", description = "Validates token signature, expiration and user existence.")
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
		Authentication result=null;
		try {
			result=authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		} catch (DisabledException e) {
			throw new Exception("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			throw new Exception("INVALID_CREDENTIALS", e);
		}
		return result;
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
