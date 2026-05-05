package org.springframework.samples.securitymicroservice.util;

import java.io.Serializable;

public class JwtValidationResponse implements Serializable {

	private static final long serialVersionUID = 7873440315174544056L;

	private final boolean valid;

	public JwtValidationResponse(boolean valid) {
		this.valid = valid;
	}

	public boolean isValid() {
		return valid;
	}

}
