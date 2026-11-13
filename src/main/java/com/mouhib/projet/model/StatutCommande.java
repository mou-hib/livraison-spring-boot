package com.mouhib.projet.model;

public enum StatutCommande {
    EN_ATTENTE,   // created, no driver yet
    EN_COURS,     // assigned to a driver, on the way
    LIVREE,       // delivered (final)
    ANNULEE;      // cancelled (final)

    // A finished order can no longer change (no cancel), but it can be deleted
    public boolean estTerminee() {
        return this == LIVREE || this == ANNULEE;
    }
}
