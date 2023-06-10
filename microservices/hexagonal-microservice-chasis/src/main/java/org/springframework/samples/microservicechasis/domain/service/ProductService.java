package org.springframework.samples.microservicechasis.domain.service;

import org.springframework.samples.microservicechasis.application.ports.input.usecases.CreateProductUseCase;
import org.springframework.samples.microservicechasis.application.ports.input.usecases.GetProductUseCase;
import org.springframework.samples.microservicechasis.application.ports.ouput.ProductEventPublisher;
import org.springframework.samples.microservicechasis.application.ports.ouput.ProductOutputPort;
import org.springframework.samples.microservicechasis.domain.event.ProductCreatedEvent;
import org.springframework.samples.microservicechasis.domain.exception.ProductNotFound;
import org.springframework.samples.microservicechasis.domain.model.Product;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ProductService  implements CreateProductUseCase, GetProductUseCase {
	private final ProductOutputPort productOutputPort;

    private final ProductEventPublisher productEventPublisher;

    @Override
    public Product createProduct(Product product) {
        product = productOutputPort.saveProduct(product);
        productEventPublisher.publishProductCreatedEvent(new ProductCreatedEvent(product.getId()));
        return product;
    }

    @Override
    public Product getProductById(Long id) {
        return productOutputPort.getProductById(id).orElseThrow(() -> new ProductNotFound("Product not found with id " + id));
    }

}
