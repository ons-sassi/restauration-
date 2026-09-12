import { Utilisateur } from './utilisateur.model';
import { StatutPresence } from './enums/statut-presence.enum';
import { Role } from './role.model';
import { PointDeVente } from './point-de-vente.model';

export interface Employee extends Utilisateur {
  matricule: string;
  date_embauche: string;
  codePin: string;
  salaire_base: number;
  statutPresence: StatutPresence;

  // Phase 3C : éligibilité à recevoir une prise en charge (auto ou
  // manuelle), distincte de l'affectation permanente d'une table
  // (TableRestaurant.serveurAttribue). Optionnel côté frontend car
  // absent pour les employés créés avant la phase 3C (traité comme
  // "false"/non éligible côté backend).
  eligibleAttributionAutomatique?: boolean;

  role: Role;
  pdvAffecte: PointDeVente;
}
