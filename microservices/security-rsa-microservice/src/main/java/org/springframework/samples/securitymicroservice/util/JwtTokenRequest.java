package org.springframework.samples.securitymicroservice.util;

import java.io.Serializable;

public class JwtTokenRequest implements Serializable {

	private static final long serialVersionUID = -4243926886918764764L;

	private String token;

	public JwtTokenRequest() {
	}

	public JwtTokenRequest(String token) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}
