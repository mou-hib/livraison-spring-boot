package com.mouhib.projet.controller;

import com.mouhib.projet.model.Commande;
import com.mouhib.projet.model.StatutCommande;
import com.mouhib.projet.service.CommandeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commandes")
public class CommandeController {

    private final CommandeService commandeService;

    public CommandeController(CommandeService commandeService) {
        this.commandeService = commandeService;
    }

    @GetMapping
    public ResponseEntity<List<Commande>> getAll() {
        return ResponseEntity.ok(commandeService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Commande> getById(@PathVariable Long id) {
        return ResponseEntity.ok(commandeService.getById(id));
    }

    // GET /api/commandes/statut/EN_ATTENTE -> custom query 3
    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<Commande>> getByStatut(@PathVariable StatutCommande statut) {
        return ResponseEntity.ok(commandeService.getByStatut(statut));
    }

    @PostMapping
    public ResponseEntity<Commande> create(@Valid @RequestBody Commande commande) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commandeService.create(commande));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Commande> update(@PathVariable Long id, @Valid @RequestBody Commande commande) {
        return ResponseEntity.ok(commandeService.update(id, commande));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commandeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- Business actions (change the status of the order) ---

    // PUT /api/commandes/1/assigner/2 -> give order 1 to driver 2
    @PutMapping("/{id}/assigner/{livreurId}")
    public ResponseEntity<Commande> assigner(@PathVariable Long id, @PathVariable Long livreurId) {
        return ResponseEntity.ok(commandeService.assignerLivreur(id, livreurId));
    }

    @PutMapping("/{id}/livrer")
    public ResponseEntity<Commande> livrer(@PathVariable Long id) {
        return ResponseEntity.ok(commandeService.livrer(id));
    }

    @PutMapping("/{id}/annuler")
    public ResponseEntity<Commande> annuler(@PathVariable Long id) {
        return ResponseEntity.ok(commandeService.annuler(id));
    }
}
