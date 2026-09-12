import { ClientAuthentifie } from './client-authentifie.model';

export interface Suggestion {
  id_suggestion: number;
  contenu: string;
  dateCreation: string;
  priseEnCompte: boolean;

  client: ClientAuthentifie;
}
