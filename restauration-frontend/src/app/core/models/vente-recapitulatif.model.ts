export type PeriodeVente = 'JOUR' | 'SEMAINE' | 'MOIS' | 'TRIMESTRE' | 'ANNEE';
export enum PeriodeRapport {
  JOUR = 'JOUR',
  SEMAINE = 'SEMAINE',
  MOIS = 'MOIS',
  TRIMESTRE = 'TRIMESTRE',
  ANNEE = 'ANNEE',
}
export interface VenteGraph {
  periode: string;
  montant: number;
}

export interface VenteRecapitulatif {
  chiffreAffaires: number;
  venteBrute: number;
  reductions: number;
  venteNette: number;
  margeBrute: number;
  evolution: VenteGraph[];
}
