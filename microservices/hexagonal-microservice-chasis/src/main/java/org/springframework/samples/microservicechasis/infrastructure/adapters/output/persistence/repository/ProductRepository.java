package org.springframework.samples.microservicechasis.infrastructure.adapters.output.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.samples.microservicechasis.infrastructure.adapters.output.persistence.entity.ProductEntity;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

	
}
