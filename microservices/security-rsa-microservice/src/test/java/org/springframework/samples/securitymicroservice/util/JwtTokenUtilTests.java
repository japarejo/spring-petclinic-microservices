package org.springframework.samples.securitymicroservice.util;

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
	void shouldGenerateAndValidateRsaSignedToken() {
		UserDetails userDetails = new User("vet1", "v3t", List.of(new SimpleGrantedAuthority("veterinarian")));

		String token = jwtTokenUtil.generateToken(userDetails);

		assertThat(jwtTokenUtil.validateTokenSignatureAndExpiration(token)).isTrue();
		assertThat(jwtTokenUtil.validateToken(token, userDetails)).isTrue();
		assertThat(jwtTokenUtil.getUsernameFromToken(token)).isEqualTo("vet1");
	}

}
