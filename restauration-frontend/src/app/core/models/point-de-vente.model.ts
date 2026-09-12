import { Restaurant } from './restaurant.model';

export interface PointDeVente {
  id_pdv: number;
  nomPdv: string;
  appareil_pos: string;
  statutConnexion: string;

  restaurant: Restaurant;
}
