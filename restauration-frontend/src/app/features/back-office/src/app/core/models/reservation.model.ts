import { StatutReservation } from './enums/statut-reservation.enum';
import { ClientAuthentifie } from './client-authentifie.model';
import { Restaurant } from './restaurant.model';
import { TableRestaurant } from './table-restaurant.model';
import { Employee } from './employee.model';

export interface Reservation {
  id_reservation: number;
  nombre_personnes: number;

  dateReservation: string;
  heureReservation: string;

  statut: StatutReservation;
  date_creation: string;
  commentaire_client: string;

  client: ClientAuthentifie;
  restaurant: Restaurant;
  table: TableRestaurant;
  confirmePar: Employee;
}
