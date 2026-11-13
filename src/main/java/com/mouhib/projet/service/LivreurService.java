package com.mouhib.projet.service;

import com.mouhib.projet.exception.BusinessException;
import com.mouhib.projet.exception.ConflictException;
import com.mouhib.projet.exception.ResourceNotFoundException;
import com.mouhib.projet.model.Livreur;
import com.mouhib.projet.model.Vehicule;
import com.mouhib.projet.repository.CommandeRepository;
import com.mouhib.projet.repository.LivreurRepository;
import com.mouhib.projet.repository.VehiculeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LivreurService {

    private final LivreurRepository livreurRepository;
    private final VehiculeRepository vehiculeRepository;
    private final CommandeRepository commandeRepository;

    public List<Livreur> getAll() {
        return livreurRepository.findAll();
    }

    public Livreur getById(Long id) {
        return livreurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur", id));
    }

    public List<Livreur> getDisponibles() {
        return livreurRepository.findByDisponibleTrue();
    }

    public Livreur create(Livreur livreur) {
        livreur.setId(null);         // always INSERT, never overwrite an existing row
        livreur.setDisponible(true); // a new driver is always available
        livreur.setVehicule(chargerVehicule(livreur.getVehicule(), null));
        return livreurRepository.save(livreur);
    }

    // "disponible" is not copied: it is managed by the order actions (assigner / livrer / annuler)
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
        if (v.getId() == null) {
            throw new BusinessException("Le vehicule doit etre donne par son id");
        }
        Vehicule vehicule = vehiculeRepository.findById(v.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicule", v.getId()));

        Livreur proprietaire = vehicule.getLivreur();
        if (proprietaire != null && !proprietaire.getId().equals(livreurId)) {
            throw new ConflictException("Ce vehicule est deja attribue a un autre livreur");
        }
        return vehicule;
    }
}
