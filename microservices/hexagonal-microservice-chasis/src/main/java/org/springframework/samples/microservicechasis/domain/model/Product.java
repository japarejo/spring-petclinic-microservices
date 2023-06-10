package org.springframework.samples.microservicechasis.domain.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Product {
	 private Long id;
	 @NotBlank
	 private String name;

	 private String description;
	 
	 @Min(0)
	 double price;
}
