package com.mouhib.projet.repository;

import com.mouhib.projet.model.Livreur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LivreurRepository extends JpaRepository<Livreur, Long> {

    // Business need: which drivers can take a new order right now?
    List<Livreur> findByDisponibleTrue();
}
