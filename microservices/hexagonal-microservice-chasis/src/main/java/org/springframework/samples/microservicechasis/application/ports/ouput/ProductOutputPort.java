package org.springframework.samples.microservicechasis.application.ports.ouput;

import java.util.Optional;

import org.springframework.samples.microservicechasis.domain.model.Product;

public interface ProductOutputPort {
	Product saveProduct(Product product);

    Optional<Product> getProductById(Long id);
}
