import { Utilisateur } from './utilisateur.model';

export interface ClientAuthentifie extends Utilisateur {
  adresse: string;
  preferences: string;
  allergie: string;
  codeParrainage: string;
  date_modification_profil: string;
}
