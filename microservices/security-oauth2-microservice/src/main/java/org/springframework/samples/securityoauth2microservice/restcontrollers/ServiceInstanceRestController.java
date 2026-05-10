package org.springframework.samples.securityoauth2microservice.restcontrollers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ServiceInstanceRestController {

	@Value("${spring.application.name}")
	private String applicationName;

	@GetMapping("/service-instances/current")
	public String serviceInstances() {
		return applicationName;
	}
}
