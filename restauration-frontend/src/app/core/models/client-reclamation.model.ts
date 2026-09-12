import { StatutReclamation } from './enums/statut-reclamation.enum';

/**
 * Vue "client" d'une réclamation (voir ClientReclamationDTO côté
 * backend) : pas de sous-objet client (le client connecté connaît
 * déjà sa propre identité) ni de CommandeDTO complet, juste l'id.
 */
export interface ClientReclamation {
  id_reclamation: number;
  sujet: string;
  description: string;
  date_creation: string;

  statut: StatutReclamation;

  reponse_employee: string | null;
  date_reponse: string | null;

  commandeId: number | null;
}

/**
 * Corps de POST /api/client/reclamations (voir
 * ClientReclamationRequestDTO côté backend).
 */
export interface ClientReclamationRequest {
  sujet: string;
  description: string;
  commandeId: number | null;
}
