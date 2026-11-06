package com.mouhib.projet.repository;

import com.mouhib.projet.model.Commande;
import com.mouhib.projet.model.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande, Long> {

    // CUSTOM QUERY 3 (derived from the method name)
    // Business need: follow orders by status, e.g. all orders EN_ATTENTE
    List<Commande> findByStatut(StatutCommande statut);

    // Business need: order history of one client
    List<Commande> findByClientId(Long clientId);

    // Used before deleting a client or a driver: do they still have orders?
    boolean existsByClientId(Long clientId);

    boolean existsByLivreurId(Long livreurId);
}
