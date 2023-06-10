package org.springframework.samples.microservicechasis.infrastructure.adapters.input.eventlistener;

import org.springframework.context.event.EventListener;
import org.springframework.samples.microservicechasis.domain.event.ProductCreatedEvent;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ProductEventListenerAdapter {

    @EventListener
    public void handle(ProductCreatedEvent event){
        log.info("Product created with id " + event.getId() + " at " + event.getDate());
    }

}