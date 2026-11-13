package com.mouhib.projet.controller;

import com.mouhib.projet.model.Categorie;
import com.mouhib.projet.service.CategorieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Status codes: 200 by default, 201 for POST, 204 for DELETE.
// Errors (400, 404, 409) are handled by GlobalExceptionHandler.
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategorieController {

    private final CategorieService categorieService;

    @GetMapping
    public List<Categorie> getAll() {
        return categorieService.getAll();
    }

    @GetMapping("/{id}")
    public Categorie getById(@PathVariable Long id) {
        return categorieService.getById(id);
    }

    // @Valid -> checks @NotBlank, @Email, @Positive... before entering the method (400 if wrong)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Categorie create(@Valid @RequestBody Categorie body) {
        return categorieService.create(body);
    }

    @PutMapping("/{id}")
    public Categorie update(@PathVariable Long id, @Valid @RequestBody Categorie body) {
        return categorieService.update(id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categorieService.delete(id);
    }
}
