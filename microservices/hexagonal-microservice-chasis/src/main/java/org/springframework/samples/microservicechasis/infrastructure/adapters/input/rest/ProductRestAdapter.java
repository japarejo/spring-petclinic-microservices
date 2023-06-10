package org.springframework.samples.microservicechasis.infrastructure.adapters.input.rest;

import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.microservicechasis.application.ports.input.usecases.CreateProductUseCase;
import org.springframework.samples.microservicechasis.application.ports.input.usecases.GetProductUseCase;
import org.springframework.samples.microservicechasis.domain.model.Product;
import org.springframework.samples.microservicechasis.infrastructure.adapters.input.rest.data.ProductDto;
import org.springframework.samples.microservicechasis.infrastructure.adapters.input.rest.data.response.ProductQueryResponse;
import org.springframework.samples.microservicechasis.infrastructure.adapters.input.rest.mapper.ProductRestMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/products")
@RequiredArgsConstructor
public class ProductRestAdapter {

    private final CreateProductUseCase createProductUseCase;

    private final GetProductUseCase getProductUseCase;

    private final ProductRestMapper productRestMapper;

    @PostMapping()
    public ResponseEntity<ProductDto> createProduct(@RequestBody @Valid ProductDto productCreateRequest){
        // Request to domain
        Product product = productRestMapper.toProduct(productCreateRequest);

        product = createProductUseCase.createProduct(product);

        // Domain to response
        return new ResponseEntity<>(productRestMapper.toProductCreateResponse(product), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id){
        Product product = getProductUseCase.getProductById(id);
        return new ResponseEntity<>(productRestMapper.toProductQueryResponse(product), HttpStatus.OK);
    }

}
