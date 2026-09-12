import { Employee } from './employee.model';
import { StatutPresence } from './enums/statut-presence.enum';

export interface Presence {
  id_presence: number;

  date: string;
  heure_arrivee: string;
  heure_depart: string;

  heures_travaillees_total: number;

  // Statut du jour marqué par le responsable (page "Présence") :
  // PRESENT / ABSENT / CONGE. Optionnel pour les anciens
  // enregistrements de pointage qui n'en ont pas.
  statut?: StatutPresence;

  employee: Employee;
}
