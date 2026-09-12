import { StatutReservation } from './enums/statut-reservation.enum';

/**
 * Vue "client" d'une réservation (voir ClientReservationDTO côté
 * backend) : pas de sous-objet client ni restaurant (le client
 * connecté connaît déjà sa propre identité et son restaurant), juste
 * le numéro de la table assignée — null tant que le restaurant n'a
 * pas encore assigné de table précise.
 */
export interface ClientReservation {
  id_reservation: number;
  nombre_personnes: number;
  dateReservation: string; // format ISO "yyyy-MM-dd"
  heureReservation: string; // format "HH:mm:ss"

  statut: StatutReservation;

  date_creation: string;
  commentaire_client: string | null;

  numeroTable: number | null;
}

/**
 * Corps de POST /api/client/reservations (voir
 * ClientReservationRequestDTO côté backend).
 */
export interface ClientReservationRequest {
  nombrePersonnes: number;
  dateReservation: string;
  heureReservation: string;
  commentaireClient: string | null;
}
