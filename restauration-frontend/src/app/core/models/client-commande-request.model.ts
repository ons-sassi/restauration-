import { ModeCommande } from './enums/mode-commande.enum';
import { StatutCommande } from './enums/statut-commande.enum';

/**
 * Corps envoyé à POST /api/client/commandes (étape 5 — panier +
 * finalisation). Ne transporte que des identifiants + quantités :
 * le prix de chaque ligne est toujours recalculé côté backend, jamais
 * fait confiance depuis Angular (voir ClientCommandeServiceImpl).
 */
export interface ClientLigneCommandeRequest {
  produitId: number;
  quantite: number;
  modificateurIds: number[];
  remarque?: string | null;
}

export interface ClientCommandeRequest {
  modeCommande: ModeCommande;
  /** Requis uniquement si modeCommande = SAISIE_MANUELLE_NUMERO_TABLE. */
  numeroTable?: number | null;
  /** Requis uniquement si modeCommande = LIVRAISON. */
  adresseLivraison?: string | null;
  lignes: ClientLigneCommandeRequest[];
}

export interface ClientLigneCommandeConfirmation {
  id_ligne_commande: number;
  nomProduit: string;
  quantite: number;
  prixUnitaire: number;
  modificateurs: string[];
  categorie?: string | null;
  sousCategorie?: string | null;
}

export interface ClientCommandeConfirmation {
  id_commande: number;
  dateCommande: string;
  statut: StatutCommande;
  modeCommande: ModeCommande;
  montant_total: number;
  numeroTable: number | null;
  adresseLivraison: string | null;
  lignes: ClientLigneCommandeConfirmation[];
}
