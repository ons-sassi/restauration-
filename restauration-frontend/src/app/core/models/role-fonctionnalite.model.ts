import { Role } from './role.model';
import { Fonctionnalite } from './fonctionnalite.model';
import { InterfaceType } from './enums/interface-type.enum';
import { Employee } from './employee.model';

export interface RoleFonctionnalite {
  id_role_fonctionnalite: number;

  role: Role;
  fonctionnalite: Fonctionnalite;

  interfaceType: InterfaceType;
  autorise: boolean;
  date_attribution: string;

  attribuePar: Employee;
}
