package org.springframework.samples.securityoauth2microservice.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

class JwtTokenUtilTests {

	private JwtTokenUtil jwtTokenUtil;

	@BeforeEach
	void setUp() throws Exception {
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();

		jwtTokenUtil = new JwtTokenUtil();
		ReflectionTestUtils.setField(jwtTokenUtil, "privateKey",
				Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
		ReflectionTestUtils.setField(jwtTokenUtil, "publicKey",
				Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
	}

	@Test
	void shouldGenerateAndValidateRsaSignedTokenWithAuthorities() {
		UserDetails userDetails = new User("owner1", "0wn3r", List.of(new SimpleGrantedAuthority("owner")));

		String token = jwtTokenUtil.generateToken(userDetails);

		assertThat(jwtTokenUtil.validateTokenSignatureAndExpiration(token)).isTrue();
		assertThat(jwtTokenUtil.validateToken(token, userDetails)).isTrue();
		assertThat(jwtTokenUtil.getUsernameFromToken(token)).isEqualTo("owner1");
	}
}
