export interface VenteParRecu {
  id_vente: number;
  numeroRecu: string;
  dateEmission: string;
  dateVente: string;

  montantTtc: number;
  modePaiement: string;
  pointDeVente: string;
}
