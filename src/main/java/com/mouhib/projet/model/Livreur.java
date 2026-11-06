package com.mouhib.projet.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Livreur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le telephone est obligatoire")
    private String telephone;

    private boolean disponible = true;

    // One driver <-> one vehicle. Creates the column "vehicule_id" in the livreur table
    @OneToOne
    @JoinColumn(name = "vehicule_id")
    private Vehicule vehicule;

    // One driver delivers many orders
    @OneToMany(mappedBy = "livreur")
    @JsonIgnore
    private List<Commande> commandes;
}
