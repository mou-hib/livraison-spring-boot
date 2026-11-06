package com.mouhib.projet.service;

import com.mouhib.projet.exception.BusinessException;
import com.mouhib.projet.exception.ResourceNotFoundException;
import com.mouhib.projet.model.*;
import com.mouhib.projet.repository.ClientRepository;
import com.mouhib.projet.repository.CommandeRepository;
import com.mouhib.projet.repository.LivreurRepository;
import com.mouhib.projet.repository.ProduitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/*
 * Life of an order:
 *   EN_ATTENTE --(assigner livreur)--> EN_COURS --(livrer)--> LIVREE
 *        \                               /
 *         \----------(annuler)----------/----> ANNULEE
 */
@Service
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final ClientRepository clientRepository;
    private final ProduitRepository produitRepository;
    private final LivreurRepository livreurRepository;

    public CommandeService(CommandeRepository commandeRepository,
                           ClientRepository clientRepository,
                           ProduitRepository produitRepository,
                           LivreurRepository livreurRepository) {
        this.commandeRepository = commandeRepository;
        this.clientRepository = clientRepository;
        this.produitRepository = produitRepository;
        this.livreurRepository = livreurRepository;
    }

    public List<Commande> getAll() {
        return commandeRepository.findAll();
    }

    public Commande getById(Long id) {
        return commandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande introuvable avec l'id " + id));
    }

    public List<Commande> getByStatut(StatutCommande statut) {
        return commandeRepository.findByStatut(statut);
    }

    // @Transactional = all or nothing.
    // If product 2 has no stock, the stock already removed from product 1 is put back automatically.
    @Transactional
    public Commande create(Commande commande) {
        if (commande.getClient().getId() == null) {
            throw new BusinessException("L'id du client est obligatoire");
        }
        Client client = clientRepository.findById(commande.getClient().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Client introuvable avec l'id " + commande.getClient().getId()));

        if (commande.getLignes() == null || commande.getLignes().isEmpty()) {
            throw new BusinessException("Une commande doit contenir au moins un produit");
        }

        commande.setClient(client);
        commande.setDateCommande(LocalDateTime.now());
        commande.setStatut(StatutCommande.EN_ATTENTE);
        commande.setLivreur(null);

        for (LigneCommande ligne : commande.getLignes()) {
            if (ligne.getProduit() == null || ligne.getProduit().getId() == null) {
                throw new BusinessException("Chaque ligne doit contenir un produit avec son id");
            }
            Produit produit = produitRepository.findById(ligne.getProduit().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable avec l'id " + ligne.getProduit().getId()));

            if (ligne.getQuantite() <= 0) {
                throw new BusinessException("La quantite doit etre positive pour le produit " + produit.getNom());
            }
            if (produit.getStock() < ligne.getQuantite()) {
                throw new BusinessException("Stock insuffisant pour " + produit.getNom()
                        + " (disponible : " + produit.getStock() + ")");
            }

            // Remove the quantity from the stock
            produit.setStock(produit.getStock() - ligne.getQuantite());
            produitRepository.save(produit);

            // Copy the current price: the order keeps this price even if the product price changes later
            ligne.setPrixUnitaire(produit.getPrix());
            ligne.setProduit(produit);
            ligne.setCommande(commande);
        }

        return commandeRepository.save(commande); // cascade -> the lines are saved too
    }

    // Only the delivery address can change, and only before a driver takes the order
    public Commande update(Long id, Commande data) {
        Commande commande = getById(id);
        if (commande.getStatut() != StatutCommande.EN_ATTENTE) {
            throw new BusinessException("Seule une commande EN_ATTENTE peut etre modifiee");
        }
        commande.setAdresseLivraison(data.getAdresseLivraison());
        return commandeRepository.save(commande);
    }

    @Transactional
    public Commande assignerLivreur(Long commandeId, Long livreurId) {
        Commande commande = getById(commandeId);
        Livreur livreur = livreurRepository.findById(livreurId)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur introuvable avec l'id " + livreurId));

        if (commande.getStatut() != StatutCommande.EN_ATTENTE) {
            throw new BusinessException("Seule une commande EN_ATTENTE peut recevoir un livreur");
        }
        if (!livreur.isDisponible()) {
            throw new BusinessException("Le livreur " + livreur.getNom() + " n'est pas disponible");
        }

        commande.setLivreur(livreur);
        commande.setStatut(StatutCommande.EN_COURS);
        livreur.setDisponible(false); // busy until the delivery is done
        livreurRepository.save(livreur);
        return commandeRepository.save(commande);
    }

    @Transactional
    public Commande livrer(Long id) {
        Commande commande = getById(id);
        if (commande.getStatut() != StatutCommande.EN_COURS) {
            throw new BusinessException("Seule une commande EN_COURS peut etre livree");
        }
        commande.setStatut(StatutCommande.LIVREE);

        Livreur livreur = commande.getLivreur();
        livreur.setDisponible(true); // free again
        livreurRepository.save(livreur);
        return commandeRepository.save(commande);
    }

    @Transactional
    public Commande annuler(Long id) {
        Commande commande = getById(id);
        if (commande.getStatut() == StatutCommande.LIVREE || commande.getStatut() == StatutCommande.ANNULEE) {
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
            commande.getLivreur().setDisponible(true);
            livreurRepository.save(commande.getLivreur());
        }

        commande.setStatut(StatutCommande.ANNULEE);
        return commandeRepository.save(commande);
    }

    // Only finished orders can be deleted (the stock is already correct)
    public void delete(Long id) {
        Commande commande = getById(id);
        if (commande.getStatut() != StatutCommande.ANNULEE && commande.getStatut() != StatutCommande.LIVREE) {
            throw new BusinessException("Annulez la commande avant de la supprimer");
        }
        commandeRepository.delete(commande); // cascade -> the lines are deleted too
    }
}
