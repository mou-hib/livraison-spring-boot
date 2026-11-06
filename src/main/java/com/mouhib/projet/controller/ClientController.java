package com.mouhib.projet.controller;

import com.mouhib.projet.model.Client;
import com.mouhib.projet.model.Commande;
import com.mouhib.projet.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public ResponseEntity<List<Client>> getAll() {
        return ResponseEntity.ok(clientService.getAll());                          // 200
    }

    @GetMapping("/{id}")
    public ResponseEntity<Client> getById(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getById(id));                       // 200 or 404
    }

    // @Valid -> checks @NotBlank, @Email... before entering the method (400 if wrong)
    @PostMapping
    public ResponseEntity<Client> create(@Valid @RequestBody Client client) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clientService.create(client)); // 201
    }

    @PutMapping("/{id}")
    public ResponseEntity<Client> update(@PathVariable Long id, @Valid @RequestBody Client client) {
        return ResponseEntity.ok(clientService.update(id, client));                // 200
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        clientService.delete(id);
        return ResponseEntity.noContent().build();                                 // 204
    }

    // GET /api/clients/1/commandes -> order history of client 1
    @GetMapping("/{id}/commandes")
    public ResponseEntity<List<Commande>> getCommandes(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getCommandes(id));
    }

    // GET /api/clients/1/total-depense -> custom query 4
    @GetMapping("/{id}/total-depense")
    public ResponseEntity<Map<String, Object>> getTotalDepense(@PathVariable Long id) {
        Map<String, Object> resultat = new HashMap<>();
        resultat.put("clientId", id);
        resultat.put("totalDepense", clientService.getTotalDepense(id));
        return ResponseEntity.ok(resultat);
    }
}
