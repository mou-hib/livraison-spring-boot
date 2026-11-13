package com.mouhib.projet.controller;

import com.mouhib.projet.model.Produit;
import com.mouhib.projet.service.ProduitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Status codes: 200 by default, 201 for POST, 204 for DELETE.
// Errors (400, 404, 409) are handled by GlobalExceptionHandler.
@RestController
@RequestMapping("/api/produits")
@RequiredArgsConstructor
public class ProduitController {

    private final ProduitService produitService;

    @GetMapping
    public List<Produit> getAll() {
        return produitService.getAll();
    }

    @GetMapping("/{id}")
    public Produit getById(@PathVariable Long id) {
        return produitService.getById(id);
    }

    // @Valid -> checks @NotBlank, @Email, @Positive... before entering the method (400 if wrong)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Produit create(@Valid @RequestBody Produit body) {
        return produitService.create(body);
    }

    @PutMapping("/{id}")
    public Produit update(@PathVariable Long id, @Valid @RequestBody Produit body) {
        return produitService.update(id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        produitService.delete(id);
    }

    // GET /api/produits/stock-faible?seuil=5 -> custom query 1
    @GetMapping("/stock-faible")
    public List<Produit> getStockFaible(@RequestParam(defaultValue = "5") int seuil) {
        return produitService.getStockFaible(seuil);
    }

    // GET /api/produits/categorie/Boissons -> custom query 2
    @GetMapping("/categorie/{nom}")
    public List<Produit> getByCategorie(@PathVariable String nom) {
        return produitService.getByCategorie(nom);
    }
}
