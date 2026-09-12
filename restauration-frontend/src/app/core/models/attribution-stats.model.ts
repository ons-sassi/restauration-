import { Employee } from './employee.model';

// Phase 3C — vue "statistiques d'attribution" côté responsable
// (§22/§23 de la spec) : pour chaque employé éligible, son nombre de
// prises en charge ACTIVES actuellement. Sert à faire comprendre au
// responsable pourquoi l'attribution automatique choisirait tel
// employé plutôt qu'un autre.
export interface AttributionStats {
  employee: Employee;
  nombrePrisesEnChargeActives: number;
  eligible: boolean;
}
