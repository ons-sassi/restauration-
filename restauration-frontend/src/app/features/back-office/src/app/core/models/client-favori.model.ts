/**
 * Un produit favori du client connecté (voir ClientFavoriDTO côté
 * backend). Les infos produit utiles à l'affichage sont déjà résolues
 * ici — pas besoin d'un aller-retour supplémentaire vers le menu pour
 * chaque favori.
 */
export interface ClientFavori {
  id_favori: number;
  produitId: number;
  nomProduit: string;
  imageProduit: string | null;
  prixProduit: number;
  disponibleProduit: boolean;
  dateAjout: string;
}
