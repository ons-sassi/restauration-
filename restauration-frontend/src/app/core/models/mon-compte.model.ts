import { Utilisateur } from './utilisateur.model';

/**
 * Modèle renvoyé par GET/PUT /api/mon-compte.
 *
 * Couvre n'importe quel type de compte connecté (Employee,
 * SuperAdmin, ClientAuthentifie) : matricule et nomRole ne sont
 * renseignés que si le compte connecté est un Employee, et restent
 * undefined sinon (ex. SuperAdmin).
 */
export interface MonCompte extends Utilisateur {
  matricule?: string;
  nomRole?: string;

  // Renseignés uniquement si le compte connecté est un
  // ClientAuthentifie ; restent undefined pour un Employee ou un
  // SuperAdmin (voir MonCompteController.toDto côté backend).
  //
  // adresse / allergie / preferences : modifiables par le client.
  adresse?: string;
  allergie?: string;
  preferences?: string;
  codeParrainage?: string;
}
