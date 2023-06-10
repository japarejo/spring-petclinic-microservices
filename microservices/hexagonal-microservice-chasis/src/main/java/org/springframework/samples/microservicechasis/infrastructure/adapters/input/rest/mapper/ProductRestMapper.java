package org.springframework.samples.microservicechasis.infrastructure.adapters.input.rest.mapper;

import org.mapstruct.Mapper;
import org.springframework.samples.microservicechasis.domain.model.Product;
import org.springframework.samples.microservicechasis.infrastructure.adapters.input.rest.data.ProductDto;

@Mapper
public interface ProductRestMapper {
	Product toProduct(ProductDto productCreateRequest);

    ProductDto toProductCreateResponse(Product product);

    ProductDto toProductQueryResponse(Product product);

}
