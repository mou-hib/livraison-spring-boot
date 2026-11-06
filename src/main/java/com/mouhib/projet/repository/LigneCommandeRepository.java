package com.mouhib.projet.repository;

import com.mouhib.projet.model.LigneCommande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LigneCommandeRepository extends JpaRepository<LigneCommande, Long> {

    // CUSTOM QUERY 4 (JPQL with calculation)
    // Business need: how much has a client spent in total?
    // COALESCE returns 0 instead of null when the client has no orders
    @Query("SELECT COALESCE(SUM(l.quantite * l.prixUnitaire), 0.0) " +
           "FROM LigneCommande l WHERE l.commande.client.id = :clientId")
    double totalDepenseParClient(@Param("clientId") Long clientId);

    // Used before deleting a product: is it in an order?
    boolean existsByProduitId(Long produitId);
}
