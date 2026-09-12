import { StatutVersement } from './enums/statut-versement.enum';
import { Employee } from './employee.model';

export interface VersementSalaire {
  id_versement: number;

  periode: string;
  montant: number;
  date_versement: string;

  statut: StatutVersement;

  employee: Employee;
}
