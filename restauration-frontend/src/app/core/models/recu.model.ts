import { Vente } from './vente.model';
import { ModeleRecu } from './modele-recu.model';

export interface Recu {
  id_recu: number;
  numeroRecu: string;
  date_emission: string;

  logo_affiche: boolean;
  entete_personnalise: string;
  pied_de_page_personnalise: string;
  commentaire_client: boolean;

  vente: Vente;
  modele: ModeleRecu;
}
