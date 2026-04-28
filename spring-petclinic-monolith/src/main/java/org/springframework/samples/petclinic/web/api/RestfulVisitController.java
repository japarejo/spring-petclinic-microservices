package org.springframework.samples.petclinic.web.api;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.service.VisitService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Visits", description = "Visit retrieval REST API")
@RestController
@RequestMapping("/api/v1/visits")
public class RestfulVisitController {

	VisitService visitService;
	
	@Autowired
	public RestfulVisitController(VisitService visitService) {
		this.visitService=visitService;
	}
	
	@Operation(summary = "Get visit by ID", description = "Returns the details for a single visit.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Visit returned successfully",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = Visit.class))),
		@ApiResponse(responseCode = "404", description = "Visit not found", content = @Content)
	})
	@GetMapping("/{id}")
	public Visit getVisit(@Parameter(description = "Visit identifier", required = true) @PathVariable("id")Integer id) {
		Optional<Visit> visit=visitService.findById(id);
		if(visit.isPresent())			
			return visit.get();
		else
			throw new ResourceNotFoundException();
	}
}
