package org.springframework.samples.microservicechasis.infrastructure.adapters.configuration;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.samples.microservicechasis.domain.service.ProductService;
import org.springframework.samples.microservicechasis.infrastructure.adapters.output.eventpublisher.ProductEventPublisherAdapter;
import org.springframework.samples.microservicechasis.infrastructure.adapters.output.persistence.ProductPersistenceAdapter;
import org.springframework.samples.microservicechasis.infrastructure.adapters.output.persistence.mapper.ProductPersistenceMapper;
import org.springframework.samples.microservicechasis.infrastructure.adapters.output.persistence.repository.ProductRepository;

@Configuration
public class AchitectureConfiguration {
	 	@Bean
	    public ProductPersistenceAdapter productPersistenceAdapter(ProductRepository productRepository, ProductPersistenceMapper productPersistenceMapper) {
	        return new ProductPersistenceAdapter(productRepository, productPersistenceMapper);
	    }

	    @Bean
	    public ProductEventPublisherAdapter productEventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher) {
	        return new ProductEventPublisherAdapter(applicationEventPublisher);
	    }

	    @Bean
	    public ProductService productService(ProductPersistenceAdapter productPersistenceAdapter, ProductEventPublisherAdapter productEventPublisherAdapter) {
	        return new ProductService(productPersistenceAdapter, productEventPublisherAdapter);
	    }
}
