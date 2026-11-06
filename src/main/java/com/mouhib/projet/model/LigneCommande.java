package com.mouhib.projet.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Link between Commande and Produit.
// It is an entity (not a simple @ManyToMany) because the link has its own data:
// the quantity and the price at the time of the order.
@Entity
@Getter
@Setter
@NoArgsConstructor
public class LigneCommande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Positive(message = "La quantite doit etre positive")
    private int quantite;

    private double prixUnitaire;

    // Many lines -> one order
    @ManyToOne
    @JoinColumn(name = "commande_id")
    @JsonIgnore
    private Commande commande;

    // Many lines -> one product
    @ManyToOne
    @JoinColumn(name = "produit_id")
    private Produit produit;
}
