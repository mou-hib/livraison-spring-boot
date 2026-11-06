package com.mouhib.projet.service;

import com.mouhib.projet.exception.BusinessException;
import com.mouhib.projet.exception.ConflictException;
import com.mouhib.projet.exception.ResourceNotFoundException;
import com.mouhib.projet.model.Livreur;
import com.mouhib.projet.model.Vehicule;
import com.mouhib.projet.repository.CommandeRepository;
import com.mouhib.projet.repository.LivreurRepository;
import com.mouhib.projet.repository.VehiculeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LivreurService {

    private final LivreurRepository livreurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final CommandeRepository commandeRepository;

    public LivreurService(LivreurRepository livreurRepository,
                          VehiculeRepository vehiculeRepository,
                          CommandeRepository commandeRepository) {
        this.livreurRepository = livreurRepository;
        this.vehiculeRepository = vehiculeRepository;
        this.commandeRepository = commandeRepository;
    }

    public List<Livreur> getAll() {
        return livreurRepository.findAll();
    }

    public Livreur getById(Long id) {
        return livreurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur introuvable avec l'id " + id));
    }

    public List<Livreur> getDisponibles() {
        return livreurRepository.findByDisponibleTrue();
    }

    public Livreur create(Livreur livreur) {
        livreur.setDisponible(true); // a new driver is always available
        livreur.setVehicule(chargerVehicule(livreur.getVehicule(), null));
        return livreurRepository.save(livreur);
    }

    public Livreur update(Long id, Livreur data) {
        Livreur livreur = getById(id);
        livreur.setNom(data.getNom());
        livreur.setTelephone(data.getTelephone());
        livreur.setVehicule(chargerVehicule(data.getVehicule(), id));
        return livreurRepository.save(livreur);
    }

    public void delete(Long id) {
        getById(id);
        if (commandeRepository.existsByLivreurId(id)) {
            throw new BusinessException("Impossible de supprimer un livreur qui a des commandes");
        }
        livreurRepository.deleteById(id);
    }

    // The JSON sends only the vehicle id: "vehicule": {"id": 1}
    // Rule: one vehicle = one driver (OneToOne)
    private Vehicule chargerVehicule(Vehicule v, Long livreurId) {
        if (v == null) {
            return null; // a driver can have no vehicle
        }
        Vehicule vehicule = vehiculeRepository.findById(v.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule introuvable avec l'id " + v.getId()));

        Livreur proprietaire = vehicule.getLivreur();
        if (proprietaire != null && !proprietaire.getId().equals(livreurId)) {
            throw new ConflictException("Ce vehicule est deja attribue a un autre livreur");
        }
        return vehicule;
    }
}
