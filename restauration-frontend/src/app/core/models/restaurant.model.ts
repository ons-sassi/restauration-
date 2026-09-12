import { Devise } from './enums/devise.enum';
import { LangueParDefaut } from './enums/langue-par-defaut.enum';
import { StatutRestaurant } from './enums/statut-restaurant.enum';

export interface Restaurant {
  id_restaurant?: number;

  nomRestaurant: string;

  adresse: string;

  logo?: string;

  devise?: Devise;

  langue_par_defaut?: LangueParDefaut;

  horaires_ouverture?: string;

  horaires_fermeture?: string;

  statut?: StatutRestaurant;
}
