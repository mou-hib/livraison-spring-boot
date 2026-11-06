package com.mouhib.projet.service;

import com.mouhib.projet.exception.BusinessException;
import com.mouhib.projet.exception.ResourceNotFoundException;
import com.mouhib.projet.model.Categorie;
import com.mouhib.projet.repository.CategorieRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategorieService {

    private final CategorieRepository categorieRepository;

    public CategorieService(CategorieRepository categorieRepository) {
        this.categorieRepository = categorieRepository;
    }

    public List<Categorie> getAll() {
        return categorieRepository.findAll();
    }

    public Categorie getById(Long id) {
        return categorieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categorie introuvable avec l'id " + id));
    }

    public Categorie create(Categorie categorie) {
        return categorieRepository.save(categorie);
    }

    public Categorie update(Long id, Categorie data) {
        Categorie categorie = getById(id);
        categorie.setNom(data.getNom());
        return categorieRepository.save(categorie);
    }

    public void delete(Long id) {
        Categorie categorie = getById(id);
        if (categorie.getProduits() != null && !categorie.getProduits().isEmpty()) {
            throw new BusinessException("Impossible de supprimer une categorie qui contient des produits");
        }
        categorieRepository.deleteById(id);
    }
}
