package org.springframework.samples.microservicechasis.application.ports.input.usecases;

import org.springframework.samples.microservicechasis.domain.model.Product;

public interface CreateProductUseCase {
	Product createProduct(Product product);
}
