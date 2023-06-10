package org.springframework.samples.microservicechasis.infrastructure.adapters.input.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
	public ResourceNotFoundException() {
		this("");
	}

	public ResourceNotFoundException(String message) {
		super(message);
	}
}
