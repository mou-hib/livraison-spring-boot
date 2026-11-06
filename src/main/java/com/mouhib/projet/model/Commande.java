package com.mouhib.projet.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateCommande;

    @Enumerated(EnumType.STRING)
    private StatutCommande statut;

    @NotBlank(message = "L'adresse de livraison est obligatoire")
    private String adresseLivraison;

    // Many orders -> one client. Creates the column "client_id"
    @NotNull(message = "Le client est obligatoire")
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    // Many orders -> one driver. Empty until a driver is assigned
    @ManyToOne
    @JoinColumn(name = "livreur_id")
    private Livreur livreur;

    // One order has many lines. cascade = saving/deleting the order also saves/deletes its lines
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL)
    private List<LigneCommande> lignes;
}
