import { StatutUtilisateur } from './enums/statut-utilisateur.enum';

export interface Utilisateur {
  id_utilisateur: number;
  nom: string;
  prenom: string;
  email: string;
  telephone: string;
  mot_de_passe: string;
  date_creation: string;
  date_derniere_connection: string;
  statut: StatutUtilisateur;
  photo_profil: string;
}
