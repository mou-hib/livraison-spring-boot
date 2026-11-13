package com.mouhib.projet.service;

import com.mouhib.projet.exception.BusinessException;
import com.mouhib.projet.exception.ConflictException;
import com.mouhib.projet.exception.ResourceNotFoundException;
import com.mouhib.projet.model.Client;
import com.mouhib.projet.model.Commande;
import com.mouhib.projet.repository.ClientRepository;
import com.mouhib.projet.repository.CommandeRepository;
import com.mouhib.projet.repository.LigneCommandeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final CommandeRepository commandeRepository;
    private final LigneCommandeRepository ligneCommandeRepository;

    public List<Client> getAll() {
        return clientRepository.findAll();
    }

    public Client getById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
    }

    public Client create(Client client) {
        if (clientRepository.existsByEmail(client.getEmail())) {
            throw new ConflictException("Un client avec cet email existe deja");
        }
        client.setId(null); // always INSERT, never overwrite an existing row
        return clientRepository.save(client);
    }

    public Client update(Long id, Client data) {
        Client client = getById(id);

        // Email changed -> the new email must not be used by someone else
        boolean emailChange = !client.getEmail().equals(data.getEmail());
        if (emailChange && clientRepository.existsByEmail(data.getEmail())) {
            throw new ConflictException("Un client avec cet email existe deja");
        }

        client.setNom(data.getNom());
        client.setEmail(data.getEmail());
        client.setTelephone(data.getTelephone());
        client.setAdresse(data.getAdresse());
        return clientRepository.save(client);
    }

    public void delete(Long id) {
        getById(id); // 404 if it does not exist
        if (commandeRepository.existsByClientId(id)) {
            throw new BusinessException("Impossible de supprimer un client qui a des commandes");
        }
        clientRepository.deleteById(id);
    }

    public List<Commande> getCommandes(Long id) {
        getById(id);
        return commandeRepository.findByClientId(id);
    }

    public double getTotalDepense(Long id) {
        getById(id);
        return ligneCommandeRepository.totalDepenseParClient(id);
    }
}
