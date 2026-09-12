import { StatutTable } from './enums/statut-table.enum';
import { Restaurant } from './restaurant.model';
import { Employee } from './employee.model';

export interface TableRestaurant {
  id_table: number;
  numeroTable: number;
  capacite: number;
  statut: StatutTable;
  codeQr: string;
  urlQr: string;
  qrActif: boolean;
  dateGenerationQr: string;

  // Position de la table dans le plan visuel du restaurant (phase 2B).
  // Peut être null tant que la table n'a pas encore été positionnée.
  positionX?: number | null;
  positionY?: number | null;

  restaurant: Restaurant;
  serveurAttribue: Employee;
  generePar: Employee;
}
