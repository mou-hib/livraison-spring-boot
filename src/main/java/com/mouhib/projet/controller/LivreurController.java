package com.mouhib.projet.controller;

import com.mouhib.projet.model.Livreur;
import com.mouhib.projet.service.LivreurService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/livreurs")
public class LivreurController {

    private final LivreurService livreurService;

    public LivreurController(LivreurService livreurService) {
        this.livreurService = livreurService;
    }

    @GetMapping
    public ResponseEntity<List<Livreur>> getAll() {
        return ResponseEntity.ok(livreurService.getAll());
    }

    // GET /api/livreurs/disponibles -> drivers free to take an order
    @GetMapping("/disponibles")
    public ResponseEntity<List<Livreur>> getDisponibles() {
        return ResponseEntity.ok(livreurService.getDisponibles());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Livreur> getById(@PathVariable Long id) {
        return ResponseEntity.ok(livreurService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Livreur> create(@Valid @RequestBody Livreur livreur) {
        return ResponseEntity.status(HttpStatus.CREATED).body(livreurService.create(livreur));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Livreur> update(@PathVariable Long id, @Valid @RequestBody Livreur livreur) {
        return ResponseEntity.ok(livreurService.update(id, livreur));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        livreurService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
