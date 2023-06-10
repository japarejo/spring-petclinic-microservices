package org.springframework.samples.microservicechasis.infrastructure.adapters.output.persistence;

import java.util.Optional;

import org.springframework.samples.microservicechasis.application.ports.ouput.ProductOutputPort;
import org.springframework.samples.microservicechasis.domain.model.Product;
import org.springframework.samples.microservicechasis.infrastructure.adapters.output.persistence.entity.ProductEntity;
import org.springframework.samples.microservicechasis.infrastructure.adapters.output.persistence.mapper.ProductPersistenceMapper;
import org.springframework.samples.microservicechasis.infrastructure.adapters.output.persistence.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductPersistenceAdapter implements ProductOutputPort {

    private final ProductRepository productRepository;

    private final ProductPersistenceMapper productPersistenceMapper;

    @Override
    public Product saveProduct(Product product) {
        ProductEntity productEntity = productPersistenceMapper.toProductEntity(product);
        productEntity = productRepository.save(productEntity);
        return productPersistenceMapper.toProduct(productEntity);
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        Optional<ProductEntity> productEntity = productRepository.findById(id);

        if(productEntity.isEmpty()) {
            return Optional.empty();
        }

        Product product = productPersistenceMapper.toProduct(productEntity.get());
        return Optional.of(product);
    }

}