package com.mouhib.projet.service;

import com.mouhib.projet.exception.BusinessException;
import com.mouhib.projet.exception.ResourceNotFoundException;
import com.mouhib.projet.model.*;
import com.mouhib.projet.repository.ClientRepository;
import com.mouhib.projet.repository.CommandeRepository;
import com.mouhib.projet.repository.LivreurRepository;
import com.mouhib.projet.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/*
 * Life of an order:
 *   EN_ATTENTE --(assigner livreur)--> EN_COURS --(livrer)--> LIVREE
 *        \                               /
 *         \----------(annuler)----------/----> ANNULEE
 *
 * @Transactional = all or nothing: if one step fails, every change already made is undone.
 */
@Service
@RequiredArgsConstructor
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final ClientRepository clientRepository;
    private final ProduitRepository produitRepository;
    private final LivreurRepository livreurRepository;

    public List<Commande> getAll() {
        return commandeRepository.findAll();
    }

    public Commande getById(Long id) {
        return commandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande", id));
    }

    public List<Commande> getByStatut(StatutCommande statut) {
        return commandeRepository.findByStatut(statut);
    }

    // If product 2 has no stock, the stock already removed from product 1 is put back automatically
    @Transactional
    public Commande create(Commande commande) {
        Long clientId = commande.getClient().getId();
        if (clientId == null) {
            throw new BusinessException("L'id du client est obligatoire");
        }
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));

        if (commande.getLignes() == null || commande.getLignes().isEmpty()) {
            throw new BusinessException("Une commande doit contenir au moins un produit");
        }

        // The server decides these values, whatever the JSON contains
        commande.setId(null);
        commande.setClient(client);
        commande.setDateCommande(LocalDateTime.now());
        commande.setStatut(StatutCommande.EN_ATTENTE);
        commande.setLivreur(null);

        for (LigneCommande ligne : commande.getLignes()) {
            preparerLigne(ligne, commande);
        }
        return commandeRepository.save(commande); // cascade -> the lines are saved too
    }

    // Only the delivery address can change, and only before a driver takes the order
    public Commande update(Long id, Commande data) {
        Commande commande = getById(id);
        verifierStatut(commande, StatutCommande.EN_ATTENTE, "Seule une commande EN_ATTENTE peut etre modifiee");
        commande.setAdresseLivraison(data.getAdresseLivraison());
        return commandeRepository.save(commande);
    }

    @Transactional
    public Commande assignerLivreur(Long commandeId, Long livreurId) {
        Commande commande = getById(commandeId);
        Livreur livreur = livreurRepository.findById(livreurId)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur", livreurId));

        verifierStatut(commande, StatutCommande.EN_ATTENTE, "Seule une commande EN_ATTENTE peut recevoir un livreur");
        if (!livreur.isDisponible()) {
            throw new BusinessException("Le livreur " + livreur.getNom() + " n'est pas disponible");
        }

        commande.setLivreur(livreur);
        commande.setStatut(StatutCommande.EN_COURS);
        changerDisponibilite(livreur, false); // busy until the delivery is done
        return commandeRepository.save(commande);
    }

    @Transactional
    public Commande livrer(Long id) {
        Commande commande = getById(id);
        verifierStatut(commande, StatutCommande.EN_COURS, "Seule une commande EN_COURS peut etre livree");

        commande.setStatut(StatutCommande.LIVREE);
        changerDisponibilite(commande.getLivreur(), true); // free again
        return commandeRepository.save(commande);
    }

    @Transactional
    public Commande annuler(Long id) {
        Commande commande = getById(id);
        if (commande.getStatut().estTerminee()) {
            throw new BusinessException("Une commande " + commande.getStatut() + " ne peut pas etre annulee");
        }

        // Put the products back in stock
        for (LigneCommande ligne : commande.getLignes()) {
            Produit produit = ligne.getProduit();
            produit.setStock(produit.getStock() + ligne.getQuantite());
            produitRepository.save(produit);
        }

        // Free the driver if there was one
        if (commande.getLivreur() != null) {
            changerDisponibilite(commande.getLivreur(), true);
        }

        commande.setStatut(StatutCommande.ANNULEE);
        return commandeRepository.save(commande);
    }

    // Only finished orders can be deleted (the stock is already correct)
    public void delete(Long id) {
        Commande commande = getById(id);
        if (!commande.getStatut().estTerminee()) {
            throw new BusinessException("Annulez la commande avant de la supprimer");
        }
        commandeRepository.delete(commande); // cascade -> the lines are deleted too
    }

    // --- Helpers ---

    // Checks one order line, removes the quantity from the stock and links the line to the order
    private void preparerLigne(LigneCommande ligne, Commande commande) {
        if (ligne == null || ligne.getProduit() == null || ligne.getProduit().getId() == null) {
            throw new BusinessException("Chaque ligne doit contenir un produit avec son id");
        }
        Long produitId = ligne.getProduit().getId();
        Produit produit = produitRepository.findById(produitId)
                .orElseThrow(() -> new ResourceNotFoundException("Produit", produitId));

        if (ligne.getQuantite() <= 0) {
            throw new BusinessException("La quantite doit etre positive pour le produit " + produit.getNom());
        }
        if (produit.getStock() < ligne.getQuantite()) {
            throw new BusinessException("Stock insuffisant pour " + produit.getNom()
                    + " (disponible : " + produit.getStock() + ")");
        }

        produit.setStock(produit.getStock() - ligne.getQuantite());
        produitRepository.save(produit);

        ligne.setId(null);
        // Copy the current price: the order keeps this price even if the product price changes later
        ligne.setPrixUnitaire(produit.getPrix());
        ligne.setProduit(produit);
        ligne.setCommande(commande);
    }

    private void verifierStatut(Commande commande, StatutCommande attendu, String message) {
        if (commande.getStatut() != attendu) {
            throw new BusinessException(message);
        }
    }

    private void changerDisponibilite(Livreur livreur, boolean disponible) {
        livreur.setDisponible(disponible);
        livreurRepository.save(livreur);
    }
}
