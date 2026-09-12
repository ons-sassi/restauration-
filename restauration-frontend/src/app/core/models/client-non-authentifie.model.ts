import { OrigineSession } from './enums/origine-session.enum';
import { Restaurant } from './restaurant.model';
import { TableRestaurant } from './table-restaurant.model';

export interface ClientNonAuthentifie {
  id_session: number;
  allergie: string;
  restaurant_choisi: string;
  origineSession: OrigineSession;
  date_debut_session: string;
  date_fin_session: string;

  restaurant: Restaurant;
  tableScannee: TableRestaurant;
}
