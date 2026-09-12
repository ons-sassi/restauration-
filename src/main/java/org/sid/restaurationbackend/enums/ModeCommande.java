package org.sid.restaurationbackend.enums;

public enum ModeCommande {
    SUR_PLACE,
    A_EMPORTER,
    EN_LIGNE,
    SCAN_QR_TABLE,
    SAISIE_MANUELLE_NUMERO_TABLE,
    // Ajouté à l'étape 5 (panier + finalisation, espace client) :
    // livraison à une adresse fournie par le client (voir
    // Commande.adresseLivraison et ClientCommandeServiceImpl).
    LIVRAISON
}
