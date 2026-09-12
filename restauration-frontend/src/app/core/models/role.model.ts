import { Employee } from './employee.model';

export interface Role {
  id_role: number;
  nom_role: string;
  description: string;
  acces_pdv: boolean;
  acces_backoffice: boolean;
  date_creation: string;

  attribuePar: Employee;
}
