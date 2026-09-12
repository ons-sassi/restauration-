import { ClientAuthentifie } from './client-authentifie.model';

export interface ModePaiement {
  id_mode_paiement: number;
  libelle: string;
  actif: boolean;

  client: ClientAuthentifie;
}
