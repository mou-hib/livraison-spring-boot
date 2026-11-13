package com.mouhib.projet.controller;

import com.mouhib.projet.model.Client;
import com.mouhib.projet.model.Commande;
import com.mouhib.projet.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Status codes: 200 by default, 201 for POST, 204 for DELETE.
// Errors (400, 404, 409) are handled by GlobalExceptionHandler.
@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public List<Client> getAll() {
        return clientService.getAll();
    }

    @GetMapping("/{id}")
    public Client getById(@PathVariable Long id) {
        return clientService.getById(id);
    }

    // @Valid -> checks @NotBlank, @Email, @Positive... before entering the method (400 if wrong)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Client create(@Valid @RequestBody Client body) {
        return clientService.create(body);
    }

    @PutMapping("/{id}")
    public Client update(@PathVariable Long id, @Valid @RequestBody Client body) {
        return clientService.update(id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        clientService.delete(id);
    }

    // GET /api/clients/1/commandes -> order history of client 1
    @GetMapping("/{id}/commandes")
    public List<Commande> getCommandes(@PathVariable Long id) {
        return clientService.getCommandes(id);
    }

    // GET /api/clients/1/total-depense -> custom query 4
    @GetMapping("/{id}/total-depense")
    public Map<String, Object> getTotalDepense(@PathVariable Long id) {
        Map<String, Object> resultat = new LinkedHashMap<>();
        resultat.put("clientId", id);
        resultat.put("totalDepense", clientService.getTotalDepense(id));
        return resultat;
    }
}
