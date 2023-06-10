package org.springframework.samples.microservicechasis.infrastructure.adapters.output.eventpublisher;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.samples.microservicechasis.application.ports.ouput.ProductEventPublisher;
import org.springframework.samples.microservicechasis.domain.event.ProductCreatedEvent;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ProductEventPublisherAdapter implements ProductEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishProductCreatedEvent(ProductCreatedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }

}
