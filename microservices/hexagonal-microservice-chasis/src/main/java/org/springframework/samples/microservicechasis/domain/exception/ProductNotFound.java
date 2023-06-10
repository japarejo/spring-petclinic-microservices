package org.springframework.samples.microservicechasis.domain.exception;

public class ProductNotFound  extends RuntimeException {

    public ProductNotFound(String message) {
        super(message);
    }

}
