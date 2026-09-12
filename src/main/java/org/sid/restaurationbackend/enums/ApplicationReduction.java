package org.sid.restaurationbackend.enums;

/**
 * Détermine sur quels produits une réduction peut s'appliquer.
 */
public enum ApplicationReduction {
    /**
     * La réduction s'applique sur tous les produits du menu.
     */
    TOUS_PRODUITS,

    /**
     * La réduction ne s'applique que sur la liste de produits
     * explicitement sélectionnée (voir Reduction#produits).
     */
    PRODUITS_SPECIFIQUES
}
