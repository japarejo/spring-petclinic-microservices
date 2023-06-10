package org.springframework.samples.microservicechasis.domain.event;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreatedEvent {

    private Long id;

    private LocalDateTime date;

    public ProductCreatedEvent(Long id) {
        this.id = id;
        this.date = LocalDateTime.now();
    }

}
