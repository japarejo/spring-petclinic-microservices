package org.springframework.samples.microservicechasis.restcontrollers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.microservicechasis.model.Disease;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.annotation.PostConstruct;
import javax.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/v2/diseases")
@Tag(name = "Enfermedades", description = "API para la gestión de enfermedades y síntomas")
public class DiseaseRestControllerSolution {

    // Lista en memoria para simular una fuente de datos.
    private List<Disease> data = new ArrayList<>();
    
    // Contador para generar identificadores únicos.
    private AtomicLong counter = new AtomicLong();

    @PostConstruct
    public void init() {
        // Inicialización de datos de ejemplo asignando identificadores únicos.
        data.add(new Disease(counter.incrementAndGet(), "Parvovirus", 5, "Enfermedad canina muy contagiosa", List.of("dog")));
        data.add(new Disease(counter.incrementAndGet(), "Gastroenteritis", 3, "Inflamación del tracto gastrointestinal", List.of("dog", "cat")));
    }

    @Operation(summary = "Obtener todas las enfermedades", description = "Retorna un listado con todas las enfermedades registradas.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Disease.class)))
    })
    @GetMapping
    public ResponseEntity<List<Disease>> getAllDiseases() {
        return ResponseEntity.ok(data);
    }

    @Operation(summary = "Obtener enfermedad por ID", description = "Retorna la enfermedad cuyo identificador coincide con el especificado en la URL.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Enfermedad encontrada",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Disease.class))),
        @ApiResponse(responseCode = "404", description = "Enfermedad no encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Disease> getDiseaseById(@PathVariable Long id) {
        Disease disease = data.stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la enfermedad con id: " + id));
        return ResponseEntity.ok(disease);
    }

    @Operation(summary = "Crear una nueva enfermedad", description = "Registra una nueva enfermedad en el sistema. Se asigna automáticamente un identificador único.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Enfermedad creada con éxito",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Disease.class))),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Disease> createDisease(@RequestBody @Valid Disease newDisease) {
        // Asignación de un identificador único antes de agregar la enfermedad.
        newDisease.setId(counter.incrementAndGet());
        data.add(newDisease);
        return ResponseEntity.status(HttpStatus.CREATED).body(newDisease);
    }

    @Operation(summary = "Actualizar una enfermedad", description = "Actualiza la información de una enfermedad existente identificada por su ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Enfermedad actualizada correctamente",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Disease.class))),
        @ApiResponse(responseCode = "404", description = "Enfermedad no encontrada", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Disease> updateDisease(@PathVariable Long id, @RequestBody @Valid Disease updatedDisease) {
        Disease disease = data.stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la enfermedad con id: " + id));
        // Se actualizan los campos correspondientes; el id permanece inalterable.
        disease.setName(updatedDisease.getName());
        disease.setSeverity(updatedDisease.getSeverity());
        disease.setDescription(updatedDisease.getDescription());
        disease.setPetTypes(updatedDisease.getPetTypes());
        return ResponseEntity.ok(disease);
    }

    @Operation(summary = "Eliminar una enfermedad", description = "Elimina la enfermedad cuyo identificador coincide con el especificado en la URL.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Enfermedad eliminada correctamente", content = @Content),
        @ApiResponse(responseCode = "404", description = "Enfermedad no encontrada", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDisease(@PathVariable Long id) {
        boolean removed = data.removeIf(d -> d.getId().equals(id));
        if (!removed) {
            throw new ResourceNotFoundException("No se encontró la enfermedad con id: " + id);
        }
        return ResponseEntity.noContent().build();
    }
}


