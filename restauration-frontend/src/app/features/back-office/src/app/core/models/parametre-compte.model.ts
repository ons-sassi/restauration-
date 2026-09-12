import { Restaurant } from './restaurant.model';

export interface ParametreCompte {
  id_parametre: number;

  langue: string;
  devise: string;
  logo: string;

  afficher_infos_client_sur_recu: boolean;
  afficher_commentaire: boolean;

  option_restauration: string;

  restaurant: Restaurant;
}
