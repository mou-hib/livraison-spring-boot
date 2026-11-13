package com.mouhib.projet.service;

import com.mouhib.projet.exception.BusinessException;
import com.mouhib.projet.exception.ResourceNotFoundException;
import com.mouhib.projet.model.Categorie;
import com.mouhib.projet.repository.CategorieRepository;
import com.mouhib.projet.repository.ProduitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor // Lombok writes the constructor -> Spring injects the repositories
public class CategorieService {

    private final CategorieRepository categorieRepository;
    private final ProduitRepository produitRepository;

    public List<Categorie> getAll() {
        return categorieRepository.findAll();
    }

    public Categorie getById(Long id) {
        return categorieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categorie", id));
    }

    public Categorie create(Categorie categorie) {
        categorie.setId(null); // always INSERT, never overwrite an existing row
        return categorieRepository.save(categorie);
    }

    public Categorie update(Long id, Categorie data) {
        Categorie categorie = getById(id);
        categorie.setNom(data.getNom());
        return categorieRepository.save(categorie);
    }

    public void delete(Long id) {
        getById(id); // 404 if it does not exist
        if (produitRepository.existsByCategoriesId(id)) {
            throw new BusinessException("Impossible de supprimer une categorie qui contient des produits");
        }
        categorieRepository.deleteById(id);
    }
}
