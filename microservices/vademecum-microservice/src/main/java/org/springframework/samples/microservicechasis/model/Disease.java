package org.springframework.samples.microservicechasis.model;

import java.util.List;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Representa una enfermedad con un identificador único, nombre, severidad, descripción y tipos de mascotas afectadas.")
public class Disease {

    @Schema(description = "Identificador único de la enfermedad", example = "1", required = true)
    private Long id;

    @Schema(description = "Nombre de la enfermedad", example = "Parvovirus", required = true)
    private String name;

    @Schema(description = "Nivel de severidad de la enfermedad (valor entre 1 y 5)", example = "5", minimum = "1", maximum = "5")
    @Min(1)
    @Max(5)
    private int severity;

    @Schema(description = "Descripción detallada de la enfermedad", example = "Enfermedad canina muy contagiosa")
    private String description;

    @Schema(description = "Lista de tipos de mascotas afectadas", example = "[\"Perro\", \"Gato\"]")
    private List<String> petTypes;
}
