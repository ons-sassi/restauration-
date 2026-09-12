import { Employee } from './employee.model';
import { Presence } from './presence.model';

// Ligne de la page "Présence" du Back Office : un employé, son
// éventuel enregistrement de présence pour le jour consulté (null si
// rien n'a encore été marqué ce jour-là) et ses compteurs d'absences
// calculés côté serveur pour le mois et l'année en cours.
export interface FeuillePresence {
  employee: Employee;
  presenceDuJour: Presence | null;
  absencesMoisCourant: number;
  absencesAnneeCourante: number;
}
