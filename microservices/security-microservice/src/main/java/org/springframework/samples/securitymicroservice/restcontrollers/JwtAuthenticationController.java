package org.springframework.samples.securitymicroservice.restcontrollers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.securitymicroservice.util.JwtRequest;
import org.springframework.samples.securitymicroservice.util.JwtResponse;
import org.springframework.samples.securitymicroservice.util.JwtTokenUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
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

	/*@Autowired
	private JwtUserDetailsService userDetailsService;*/

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

}
