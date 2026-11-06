# API REST — Plateforme de livraison (DS1)

## 1. Contexte

API REST développée avec **Spring Boot 3.5.7** (Java 21, Maven) pour gérer une plateforme de livraison :
catalogue de produits classés par catégories, clients qui passent des commandes, livreurs équipés
d'un véhicule qui prennent en charge et livrent ces commandes.

Les données sont stockées dans **MariaDB** (driver `mariadb-java-client`) via Spring Data JPA / Hibernate.
L'architecture suit le découpage classique : `model` → `repository` → `service` → `controller`,
avec un `GlobalExceptionHandler` qui transforme les erreurs en réponses JSON avec le bon code HTTP.

## 2. Les 7 entités et leurs relations

| Entité          | Champs principaux                                                        |
|-----------------|--------------------------------------------------------------------------|
| `Categorie`     | id, nom (unique)                                                         |
| `Produit`       | id, nom, prix (> 0), stock (≥ 0), categories                             |
| `Client`        | id, nom, email (unique, valide), telephone, adresse                      |
| `Commande`      | id, dateCommande, statut, adresseLivraison, client, livreur, lignes      |
| `LigneCommande` | id, quantite (> 0), prixUnitaire, commande, produit                      |
| `Livreur`       | id, nom, telephone, disponible, vehicule                                 |
| `Vehicule`      | id, immatriculation (unique), type (`VELO`, `MOTO`, `VOITURE`), capaciteKg |

Enum `StatutCommande` : `EN_ATTENTE`, `EN_COURS`, `LIVREE`, `ANNULEE`.

### Relations

- **Produit ⟷ Categorie** : `@ManyToMany` (table de jointure `produit_categorie`, Produit est propriétaire).
- **Client → Commande** : `@OneToMany` / `@ManyToOne` (colonne `client_id`).
- **Livreur → Commande** : `@OneToMany` / `@ManyToOne` (colonne `livreur_id`, vide tant qu'aucun livreur n'est assigné).
- **Commande → LigneCommande** : `@OneToMany` avec `cascade = ALL` (colonne `commande_id`).
- **LigneCommande → Produit** : `@ManyToOne` (colonne `produit_id`).
  `LigneCommande` est une entité à part entière (et non un simple `@ManyToMany` Commande–Produit)
  car le lien porte ses propres données : la quantité et le prix au moment de la commande.
- **Livreur ⟷ Vehicule** : `@OneToOne` (colonne `vehicule_id` dans `livreur`, Livreur est propriétaire).

### Diagramme

```
                 N        produit_categorie        N
 ┌───────────┐ ──────────────────────────────────── ┌──────────┐
 │ Categorie │                                      │ Produit  │
 └───────────┘                                      └──────────┘
                                                         │ 1
                                                         │
                                                         │ N
 ┌──────────┐ 1        N ┌──────────┐ 1        N ┌───────────────┐
 │  Client  │ ────────── │ Commande │ ────────── │ LigneCommande │
 └──────────┘            └──────────┘            └───────────────┘
                              │ N
                              │
                              │ 1
                         ┌──────────┐ 1        1 ┌──────────┐
                         │ Livreur  │ ────────── │ Vehicule │
                         └──────────┘            └──────────┘
```

## 3. Requêtes personnalisées

| # | Repository                | Méthode                                  | Type                        | Besoin métier                                   |
|---|---------------------------|------------------------------------------|-----------------------------|-------------------------------------------------|
| 1 | `ProduitRepository`       | `findByStockLessThan(int seuil)`         | Dérivée du nom de méthode   | Alerte stock : produits dont le stock < seuil   |
| 2 | `ProduitRepository`       | `findByCategorie(String nomCategorie)`   | JPQL avec `JOIN` sur le ManyToMany | Produits d'une catégorie (ex. « Boissons ») |
| 3 | `CommandeRepository`      | `findByStatut(StatutCommande statut)`    | Dérivée du nom de méthode   | Suivre les commandes par statut                 |
| 4 | `LigneCommandeRepository` | `totalDepenseParClient(Long clientId)`   | JPQL avec calcul (`SUM`, `COALESCE`) | Montant total dépensé par un client     |

Requête 2 :
```sql
SELECT p FROM Produit p JOIN p.categories c WHERE c.nom = :nomCategorie
```

Requête 4 :
```sql
SELECT COALESCE(SUM(l.quantite * l.prixUnitaire), 0.0)
FROM LigneCommande l WHERE l.commande.client.id = :clientId
```

Autres méthodes dérivées utilisées par les services : `existsByEmail`, `findByClientId`,
`existsByClientId`, `existsByLivreurId`, `existsByProduitId`, `findByDisponibleTrue`.

## 4. Règles métier

**Cycle de vie d'une commande**

```
EN_ATTENTE --(assigner livreur)--> EN_COURS --(livrer)--> LIVREE
     \                                /
      \-----------(annuler)----------/------> ANNULEE
```

**Création d'une commande** (`@Transactional`, tout ou rien)
- Le client (par son id) est obligatoire et doit exister.
- La commande doit contenir au moins une ligne ; chaque ligne doit référencer un produit existant par son id.
- La quantité doit être positive et le stock suffisant, sinon la commande entière est refusée.
- Le stock de chaque produit est diminué de la quantité commandée.
- Le prix unitaire est copié depuis le produit : la commande garde ce prix même si le prix du produit change ensuite.
- Le statut est forcé à `EN_ATTENTE`, la date à maintenant, et aucun livreur n'est assigné.

**Actions sur une commande**
- Modification : seule l'adresse de livraison peut changer, et seulement si la commande est `EN_ATTENTE`.
- Assignation : seule une commande `EN_ATTENTE` peut recevoir un livreur, et le livreur doit être disponible.
  La commande passe `EN_COURS` et le livreur devient indisponible.
- Livraison : seule une commande `EN_COURS` peut être livrée. Elle passe `LIVREE` et le livreur redevient disponible.
- Annulation : impossible si la commande est `LIVREE` ou `ANNULEE`. Les produits sont remis en stock
  et le livreur éventuel redevient disponible.
- Suppression : seule une commande `LIVREE` ou `ANNULEE` peut être supprimée (les lignes sont supprimées en cascade).

**Autres règles**
- Email client unique (création et modification).
- Nom de catégorie et immatriculation de véhicule uniques (contrainte en base).
- Un nouveau livreur est toujours disponible ; un véhicule ne peut être attribué qu'à un seul livreur.
- Suppressions protégées : impossible de supprimer un client ou un livreur qui a des commandes,
  un produit déjà commandé, une catégorie qui contient des produits, ou un véhicule utilisé par un livreur.

## 5. Codes HTTP

| Code | Cas                                                                                                   |
|------|-------------------------------------------------------------------------------------------------------|
| 200  | Lecture, modification ou action métier réussie                                                        |
| 201  | Création réussie (`POST`)                                                                             |
| 204  | Suppression réussie (`DELETE`)                                                                        |
| 400  | Validation `@Valid` échouée (liste des champs en erreur), règle métier violée, JSON invalide ou valeur d'enum inconnue, paramètre d'URL invalide |
| 404  | Ressource introuvable (id inexistant)                                                                 |
| 409  | Donnée en double (email, véhicule déjà attribué) ou opération refusée par la base (contrainte unique / clé étrangère) |

Format d'une réponse d'erreur :
```json
{
  "timestamp": "2026-11-06T10:15:30",
  "status": 404,
  "message": "Client introuvable avec l'id 99"
}
```

Pour une erreur de validation, un champ `erreurs` est ajouté avec le message de chaque champ invalide.

## 6. Endpoints

URL de base : `http://localhost:8080`

### Catégories — `/api/categories`
| Méthode | URL                    | Description                |
|---------|------------------------|----------------------------|
| GET     | `/api/categories`      | Lister les catégories      |
| GET     | `/api/categories/{id}` | Catégorie par id           |
| POST    | `/api/categories`      | Créer une catégorie        |
| PUT     | `/api/categories/{id}` | Modifier une catégorie     |
| DELETE  | `/api/categories/{id}` | Supprimer une catégorie    |

### Produits — `/api/produits`
| Méthode | URL                                    | Description                                   |
|---------|----------------------------------------|-----------------------------------------------|
| GET     | `/api/produits`                        | Lister les produits                           |
| GET     | `/api/produits/{id}`                   | Produit par id                                |
| POST    | `/api/produits`                        | Créer un produit                              |
| PUT     | `/api/produits/{id}`                   | Modifier un produit                           |
| DELETE  | `/api/produits/{id}`                   | Supprimer un produit                          |
| GET     | `/api/produits/stock-faible?seuil=5`   | Requête 1 : stock < seuil (défaut : 5)        |
| GET     | `/api/produits/categorie/{nom}`        | Requête 2 : produits d'une catégorie          |

### Clients — `/api/clients`
| Méthode | URL                                | Description                               |
|---------|------------------------------------|-------------------------------------------|
| GET     | `/api/clients`                     | Lister les clients                        |
| GET     | `/api/clients/{id}`                | Client par id                             |
| POST    | `/api/clients`                     | Créer un client                           |
| PUT     | `/api/clients/{id}`                | Modifier un client                        |
| DELETE  | `/api/clients/{id}`                | Supprimer un client                       |
| GET     | `/api/clients/{id}/commandes`      | Historique des commandes du client        |
| GET     | `/api/clients/{id}/total-depense`  | Requête 4 : total dépensé par le client   |

### Véhicules — `/api/vehicules`
| Méthode | URL                    | Description              |
|---------|------------------------|--------------------------|
| GET     | `/api/vehicules`       | Lister les véhicules     |
| GET     | `/api/vehicules/{id}`  | Véhicule par id          |
| POST    | `/api/vehicules`       | Créer un véhicule        |
| PUT     | `/api/vehicules/{id}`  | Modifier un véhicule     |
| DELETE  | `/api/vehicules/{id}`  | Supprimer un véhicule    |

### Livreurs — `/api/livreurs`
| Méthode | URL                        | Description                 |
|---------|----------------------------|-----------------------------|
| GET     | `/api/livreurs`            | Lister les livreurs         |
| GET     | `/api/livreurs/disponibles`| Livreurs disponibles        |
| GET     | `/api/livreurs/{id}`       | Livreur par id              |
| POST    | `/api/livreurs`            | Créer un livreur            |
| PUT     | `/api/livreurs/{id}`       | Modifier un livreur         |
| DELETE  | `/api/livreurs/{id}`       | Supprimer un livreur        |

### Commandes — `/api/commandes`
| Méthode | URL                                      | Description                                       |
|---------|------------------------------------------|---------------------------------------------------|
| GET     | `/api/commandes`                         | Lister les commandes                              |
| GET     | `/api/commandes/{id}`                    | Commande par id                                   |
| GET     | `/api/commandes/statut/{statut}`         | Requête 3 : commandes par statut (ex. `EN_ATTENTE`) |
| POST    | `/api/commandes`                         | Créer une commande                                |
| PUT     | `/api/commandes/{id}`                    | Modifier l'adresse de livraison                   |
| DELETE  | `/api/commandes/{id}`                    | Supprimer une commande (LIVREE ou ANNULEE)        |
| PUT     | `/api/commandes/{id}/assigner/{livreurId}` | Assigner un livreur                             |
| PUT     | `/api/commandes/{id}/livrer`             | Marquer la commande comme livrée                  |
| PUT     | `/api/commandes/{id}/annuler`            | Annuler la commande (stock rendu)                 |

## 7. Exemple : créer une commande

`POST /api/commandes`

```json
{
  "adresseLivraison": "12 rue de la Liberté, Tunis",
  "client": { "id": 1 },
  "lignes": [
    { "produit": { "id": 1 }, "quantite": 2 },
    { "produit": { "id": 2 }, "quantite": 1 }
  ]
}
```

Seuls les ids sont envoyés : le service charge le client et les produits depuis la base, calcule le
prix unitaire, décrémente le stock et renvoie la commande créée (`201`) avec le statut `EN_ATTENTE`.

De la même façon, un produit référence ses catégories par id (`"categories": [{"id": 1}]`)
et un livreur son véhicule par id (`"vehicule": {"id": 1}`).

## 8. Lancer le projet

### Prérequis
- Java 21 (Lombok ne fonctionne pas avec des versions plus récentes du JDK)
- Maven
- MariaDB démarré

### Créer la base et l'utilisateur MariaDB

```sql
CREATE DATABASE livraison_db;
CREATE USER 'livraison'@'localhost' IDENTIFIED BY 'livraison123';
GRANT ALL PRIVILEGES ON livraison_db.* TO 'livraison'@'localhost';
FLUSH PRIVILEGES;
```

(à exécuter avec `sudo mariadb` ou `mariadb -u root -p`). Les tables sont créées automatiquement
par Hibernate au démarrage (`spring.jpa.hibernate.ddl-auto=update`).

### Démarrer l'application

```bash
mvn clean spring-boot:run
```

L'API écoute sur `http://localhost:8080`.

## 9. Tests avec Postman

1. Importer `Livraison_API.postman_collection.json` dans Postman.
2. Partir d'une **base vide** (les ids sont enregistrés dans les variables de la collection au fil des
   requêtes, et certains tests vérifient des valeurs précises comme le stock). Pour vider la base :
   ```sql
   DROP DATABASE livraison_db;
   CREATE DATABASE livraison_db;
   ```
   puis relancer l'application.
3. Lancer la collection complète avec le **Collection Runner**, dans l'ordre des dossiers :
   1. Catégories, 2. Produits, 3. Clients, 4. Véhicules, 5. Livreurs,
   6. Commandes (cycle complet), 7. Suppressions protégées.

La collection couvre les cas nominaux, les 4 requêtes personnalisées et les cas d'erreur (400, 404, 409).

## 10. Captures d'écran

Les captures d'écran des tests sont dans le dossier [`captures/`](captures/).
