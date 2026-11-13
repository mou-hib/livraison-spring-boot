package com.mouhib.projet.controller;

import com.mouhib.projet.model.Vehicule;
import com.mouhib.projet.service.VehiculeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Status codes: 200 by default, 201 for POST, 204 for DELETE.
// Errors (400, 404, 409) are handled by GlobalExceptionHandler.
@RestController
@RequestMapping("/api/vehicules")
@RequiredArgsConstructor
public class VehiculeController {

    private final VehiculeService vehiculeService;

    @GetMapping
    public List<Vehicule> getAll() {
        return vehiculeService.getAll();
    }

    @GetMapping("/{id}")
    public Vehicule getById(@PathVariable Long id) {
        return vehiculeService.getById(id);
    }

    // @Valid -> checks @NotBlank, @Email, @Positive... before entering the method (400 if wrong)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vehicule create(@Valid @RequestBody Vehicule body) {
        return vehiculeService.create(body);
    }

    @PutMapping("/{id}")
    public Vehicule update(@PathVariable Long id, @Valid @RequestBody Vehicule body) {
        return vehiculeService.update(id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        vehiculeService.delete(id);
    }
}
