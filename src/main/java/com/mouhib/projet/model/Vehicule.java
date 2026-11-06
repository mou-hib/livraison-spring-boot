package com.mouhib.projet.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Vehicule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "L'immatriculation est obligatoire")
    @Column(unique = true)
    private String immatriculation;

    @NotNull(message = "Le type est obligatoire")
    @Enumerated(EnumType.STRING)
    private TypeVehicule type;

    @Positive(message = "La capacite doit etre positive")
    private int capaciteKg;

    // One vehicle <-> one driver (Livreur owns the relation)
    @OneToOne(mappedBy = "vehicule")
    @JsonIgnore
    private Livreur livreur;
}
