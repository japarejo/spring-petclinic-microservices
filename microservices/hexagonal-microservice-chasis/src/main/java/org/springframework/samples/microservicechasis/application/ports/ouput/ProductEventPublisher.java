package org.springframework.samples.microservicechasis.application.ports.ouput;

import org.springframework.samples.microservicechasis.domain.event.ProductCreatedEvent;

public interface ProductEventPublisher {

    void publishProductCreatedEvent(ProductCreatedEvent event);

}
