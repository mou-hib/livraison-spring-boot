package com.mouhib.projet.repository;

import com.mouhib.projet.model.Produit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProduitRepository extends JpaRepository<Produit, Long> {

    // CUSTOM QUERY 1 (derived from the method name)
    // Business need: stock alert -> products with stock below a limit
    List<Produit> findByStockLessThan(int seuil);

    // CUSTOM QUERY 2 (JPQL with a JOIN on the ManyToMany)
    // Business need: list the products of one category, e.g. "Boissons"
    @Query("SELECT p FROM Produit p JOIN p.categories c WHERE c.nom = :nomCategorie")
    List<Produit> findByCategorie(@Param("nomCategorie") String nomCategorie);

    // Used before deleting a category: does it still contain products?
    boolean existsByCategoriesId(Long categorieId);
}
