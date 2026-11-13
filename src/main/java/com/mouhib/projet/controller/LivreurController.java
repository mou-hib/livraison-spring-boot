package com.mouhib.projet.controller;

import com.mouhib.projet.model.Livreur;
import com.mouhib.projet.service.LivreurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Status codes: 200 by default, 201 for POST, 204 for DELETE.
// Errors (400, 404, 409) are handled by GlobalExceptionHandler.
@RestController
@RequestMapping("/api/livreurs")
@RequiredArgsConstructor
public class LivreurController {

    private final LivreurService livreurService;

    @GetMapping
    public List<Livreur> getAll() {
        return livreurService.getAll();
    }

    @GetMapping("/{id}")
    public Livreur getById(@PathVariable Long id) {
        return livreurService.getById(id);
    }

    // @Valid -> checks @NotBlank, @Email, @Positive... before entering the method (400 if wrong)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Livreur create(@Valid @RequestBody Livreur body) {
        return livreurService.create(body);
    }

    @PutMapping("/{id}")
    public Livreur update(@PathVariable Long id, @Valid @RequestBody Livreur body) {
        return livreurService.update(id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        livreurService.delete(id);
    }

    // GET /api/livreurs/disponibles -> drivers free to take an order
    @GetMapping("/disponibles")
    public List<Livreur> getDisponibles() {
        return livreurService.getDisponibles();
    }
}
