package com.mouhib.projet.repository;

import com.mouhib.projet.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {

    // Used by the service to refuse a duplicate email
    boolean existsByEmail(String email);
}
