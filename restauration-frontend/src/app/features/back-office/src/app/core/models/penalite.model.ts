import { Employee } from './employee.model';

export interface Penalite {
  id_penalite: number;

  motif: string;
  montant_deduit: number;
  date: string;

  employee: Employee;
}
