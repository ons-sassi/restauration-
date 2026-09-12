import { Restaurant } from './restaurant.model';

export interface PlanDeSalle {
  id_plan: number;
  disposition_tables: string;
  dateMiseAJour: string;

  restaurant: Restaurant;
}
