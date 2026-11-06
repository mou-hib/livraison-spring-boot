package com.mouhib.projet.model;

public enum StatutCommande {
    EN_ATTENTE,   // created, no driver yet
    EN_COURS,     // assigned to a driver, on the way
    LIVREE,       // delivered (final)
    ANNULEE       // cancelled (final)
}
