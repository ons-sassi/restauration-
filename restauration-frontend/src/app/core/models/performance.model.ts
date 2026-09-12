import { Employee } from './employee.model';

export interface Performance {
  id_performance: number;

  periode: string;
  nombre_ventes: number;
  chiffre_affaire_genere: number;
  note_evaluation: number;

  employee: Employee;
}
