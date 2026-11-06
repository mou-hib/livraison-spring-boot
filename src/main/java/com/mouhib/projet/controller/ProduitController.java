package com.mouhib.projet.controller;

import com.mouhib.projet.model.Produit;
import com.mouhib.projet.service.ProduitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produits")
public class ProduitController {

    private final ProduitService produitService;

    public ProduitController(ProduitService produitService) {
        this.produitService = produitService;
    }

    @GetMapping
    public ResponseEntity<List<Produit>> getAll() {
        return ResponseEntity.ok(produitService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Produit> getById(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Produit> create(@Valid @RequestBody Produit produit) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produitService.create(produit));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Produit> update(@PathVariable Long id, @Valid @RequestBody Produit produit) {
        return ResponseEntity.ok(produitService.update(id, produit));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        produitService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/produits/stock-faible?seuil=5 -> custom query 1
    @GetMapping("/stock-faible")
    public ResponseEntity<List<Produit>> getStockFaible(@RequestParam(defaultValue = "5") int seuil) {
        return ResponseEntity.ok(produitService.getStockFaible(seuil));
    }

    // GET /api/produits/categorie/Boissons -> custom query 2
    @GetMapping("/categorie/{nom}")
    public ResponseEntity<List<Produit>> getByCategorie(@PathVariable String nom) {
        return ResponseEntity.ok(produitService.getByCategorie(nom));
    }
}
