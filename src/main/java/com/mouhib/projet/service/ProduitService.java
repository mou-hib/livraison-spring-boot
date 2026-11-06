package com.mouhib.projet.service;

import com.mouhib.projet.exception.BusinessException;
import com.mouhib.projet.exception.ResourceNotFoundException;
import com.mouhib.projet.model.Categorie;
import com.mouhib.projet.model.Produit;
import com.mouhib.projet.repository.CategorieRepository;
import com.mouhib.projet.repository.LigneCommandeRepository;
import com.mouhib.projet.repository.ProduitRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProduitService {

    private final ProduitRepository produitRepository;
    private final CategorieRepository categorieRepository;
    private final LigneCommandeRepository ligneCommandeRepository;

    public ProduitService(ProduitRepository produitRepository,
                          CategorieRepository categorieRepository,
                          LigneCommandeRepository ligneCommandeRepository) {
        this.produitRepository = produitRepository;
        this.categorieRepository = categorieRepository;
        this.ligneCommandeRepository = ligneCommandeRepository;
    }

    public List<Produit> getAll() {
        return produitRepository.findAll();
    }

    public Produit getById(Long id) {
        return produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable avec l'id " + id));
    }

    public Produit create(Produit produit) {
        produit.setCategories(chargerCategories(produit.getCategories()));
        return produitRepository.save(produit);
    }

    public Produit update(Long id, Produit data) {
        Produit produit = getById(id);
        produit.setNom(data.getNom());
        produit.setPrix(data.getPrix());
        produit.setStock(data.getStock());
        produit.setCategories(chargerCategories(data.getCategories()));
        return produitRepository.save(produit);
    }

    public void delete(Long id) {
        getById(id);
        if (ligneCommandeRepository.existsByProduitId(id)) {
            throw new BusinessException("Impossible de supprimer un produit deja commande");
        }
        produitRepository.deleteById(id);
    }

    public List<Produit> getStockFaible(int seuil) {
        return produitRepository.findByStockLessThan(seuil);
    }

    public List<Produit> getByCategorie(String nomCategorie) {
        return produitRepository.findByCategorie(nomCategorie);
    }

    // The JSON only sends the category ids: [{"id": 1}, {"id": 2}]
    // We load the real categories from the database (404 if an id does not exist)
    private List<Categorie> chargerCategories(List<Categorie> categories) {
        List<Categorie> resultat = new ArrayList<>();
        if (categories == null) {
            return resultat;
        }
        for (Categorie c : categories) {
            Categorie categorie = categorieRepository.findById(c.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Categorie introuvable avec l'id " + c.getId()));
            resultat.add(categorie);
        }
        return resultat;
    }
}
