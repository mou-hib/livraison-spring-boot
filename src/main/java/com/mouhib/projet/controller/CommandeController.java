package com.mouhib.projet.controller;

import com.mouhib.projet.model.Commande;
import com.mouhib.projet.model.StatutCommande;
import com.mouhib.projet.service.CommandeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Status codes: 200 by default, 201 for POST, 204 for DELETE.
// Errors (400, 404, 409) are handled by GlobalExceptionHandler.
@RestController
@RequestMapping("/api/commandes")
@RequiredArgsConstructor
public class CommandeController {

    private final CommandeService commandeService;

    @GetMapping
    public List<Commande> getAll() {
        return commandeService.getAll();
    }

    @GetMapping("/{id}")
    public Commande getById(@PathVariable Long id) {
        return commandeService.getById(id);
    }

    // @Valid -> checks @NotBlank, @Email, @Positive... before entering the method (400 if wrong)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Commande create(@Valid @RequestBody Commande body) {
        return commandeService.create(body);
    }

    @PutMapping("/{id}")
    public Commande update(@PathVariable Long id, @Valid @RequestBody Commande body) {
        return commandeService.update(id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        commandeService.delete(id);
    }

    // GET /api/commandes/statut/EN_ATTENTE -> custom query 3
    @GetMapping("/statut/{statut}")
    public List<Commande> getByStatut(@PathVariable StatutCommande statut) {
        return commandeService.getByStatut(statut);
    }

    // --- Business actions (change the status of the order) ---

    // PUT /api/commandes/1/assigner/2 -> give order 1 to driver 2
    @PutMapping("/{id}/assigner/{livreurId}")
    public Commande assigner(@PathVariable Long id, @PathVariable Long livreurId) {
        return commandeService.assignerLivreur(id, livreurId);
    }

    @PutMapping("/{id}/livrer")
    public Commande livrer(@PathVariable Long id) {
        return commandeService.livrer(id);
    }

    @PutMapping("/{id}/annuler")
    public Commande annuler(@PathVariable Long id) {
        return commandeService.annuler(id);
    }
}
