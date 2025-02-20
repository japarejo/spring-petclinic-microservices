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
@RequestMapping("/api/v1/diseases")
public class DiseaseRestController {

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
    
    @GetMapping
    public List<Disease> getAllDiseases() {
        return data;
    }

    @GetMapping("/{id}")
    public Disease getDiseaseById(@PathVariable Long id) {
        Disease disease = data.stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la enfermedad con id: " + id));
        return disease;
    }
    
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Disease createDisease(@RequestBody @Valid Disease newDisease) {
        // Asignación de un identificador único antes de agregar la enfermedad.
        newDisease.setId(counter.incrementAndGet());
        data.add(newDisease);
        return newDisease;
    }

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

    @DeleteMapping("/{id}")
    public void deleteDisease(@PathVariable Long id) {
        boolean removed = data.removeIf(d -> d.getId().equals(id));
        if (!removed) 
            throw new ResourceNotFoundException("No se encontró la enfermedad con id: " + id);        
    }
}


