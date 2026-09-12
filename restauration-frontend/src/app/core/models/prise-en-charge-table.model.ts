import { StatutPriseEnCharge } from './enums/statut-prise-en-charge.enum';
import { TableRestaurant } from './table-restaurant.model';
import { Employee } from './employee.model';

// Prise en charge TEMPORAIRE d'une table par un employé pendant la
// présence d'un client (phase 3B). À distinguer de l'affectation
// PERMANENTE (TableRestaurant.serveurAttribue), qui ne change pas
// lorsque le client part.
export interface PriseEnChargeTable {
  id_prise_en_charge: number;

  table: TableRestaurant;
  employee: Employee;

  dateDebut: string;
  dateFin: string | null;

  statut: StatutPriseEnCharge;
}
