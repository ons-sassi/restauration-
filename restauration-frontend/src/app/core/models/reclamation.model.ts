import { StatutReclamation } from './enums/statut-reclamation.enum';
import { ClientAuthentifie } from './client-authentifie.model';
import { Commande } from './commande.model';

export interface Reclamation {
  id_reclamation: number;
  sujet: string;
  description: string;
  date_creation: string;

  statut: StatutReclamation;

  reponse_employee: string;
  date_reponse: string;

  client: ClientAuthentifie;
  commande: Commande;
}
