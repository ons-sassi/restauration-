package org.sid.restaurationbackend.exceptions;

/**
 * Levée quand on tente de supprimer une catégorie qui contient encore
 * au moins un élément de menu (produit ou sous-catégorie) — contrainte FK
 * element_menu.id_categorie_parent. Il faut d'abord déplacer/supprimer
 * ces éléments (ou les réaffecter à une autre catégorie) avant de
 * supprimer la catégorie elle-même.
 */
public class CategorieEnUtilisationException extends Exception {
    public CategorieEnUtilisationException(String message) {
        super(message);
    }
}