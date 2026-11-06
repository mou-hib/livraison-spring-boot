package com.mouhib.projet.service;

import com.mouhib.projet.exception.BusinessException;
import com.mouhib.projet.exception.ResourceNotFoundException;
import com.mouhib.projet.model.Vehicule;
import com.mouhib.projet.repository.VehiculeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehiculeService {

    private final VehiculeRepository vehiculeRepository;

    public VehiculeService(VehiculeRepository vehiculeRepository) {
        this.vehiculeRepository = vehiculeRepository;
    }

    public List<Vehicule> getAll() {
        return vehiculeRepository.findAll();
    }

    public Vehicule getById(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule introuvable avec l'id " + id));
    }

    public Vehicule create(Vehicule vehicule) {
        return vehiculeRepository.save(vehicule);
    }

    public Vehicule update(Long id, Vehicule data) {
        Vehicule vehicule = getById(id);
        vehicule.setImmatriculation(data.getImmatriculation());
        vehicule.setType(data.getType());
        vehicule.setCapaciteKg(data.getCapaciteKg());
        return vehiculeRepository.save(vehicule);
    }

    public void delete(Long id) {
        Vehicule vehicule = getById(id);
        if (vehicule.getLivreur() != null) {
            throw new BusinessException("Impossible de supprimer un vehicule utilise par un livreur");
        }
        vehiculeRepository.deleteById(id);
    }
}
