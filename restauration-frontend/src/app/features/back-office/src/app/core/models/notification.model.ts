import { TypeNotification } from './enums/type-notification.enum';
import { StatutEnvoi } from './enums/statut-envoi.enum';
import { Utilisateur } from './utilisateur.model';

export interface Notification {
  id_notification: number;

  type: TypeNotification;
  contenu: string;
  dateEnvoi: string;
  statutEnvoi: StatutEnvoi;
  declencheur: string;

  destinataire: Utilisateur;
  emetteur: Utilisateur;
}
