package org.sid.restaurationbackend.exceptions;

/**
 * Levée quand on tente de supprimer un produit qui est encore référencé
 * par au moins une ligne de commande (contrainte FK ligne_commande.id_produit).
 * On ne supprime jamais un produit historisé dans des commandes : il faut
 * le désactiver (disponible = false) via updateProduit à la place.
 */
public class ProduitEnUtilisationException extends Exception {
    public ProduitEnUtilisationException(String message) {
        super(message);
    }
}