package org.springframework.samples.microservicechasis.infrastructure.adapters.output.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.samples.microservicechasis.domain.model.Product;
import org.springframework.samples.microservicechasis.infrastructure.adapters.configuration.MapperSpringConfig;
import org.springframework.samples.microservicechasis.infrastructure.adapters.output.persistence.entity.ProductEntity;

@Mapper(config = MapperSpringConfig.class )
public interface ProductPersistenceMapper {
	
	Product toProduct(ProductEntity productEntity);

	ProductEntity toProductEntity(Product product);

}
