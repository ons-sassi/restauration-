import { ModeCommande } from './enums/mode-commande.enum';
import { StatutCommande } from './enums/statut-commande.enum';
import { ClientAuthentifie } from './client-authentifie.model';
import { ClientNonAuthentifie } from './client-non-authentifie.model';
import { TableRestaurant } from './table-restaurant.model';
import { Employee } from './employee.model';

export interface Commande {
  id_commande: number;
  dateCommande: string;
  statut: StatutCommande;
  modeCommande: ModeCommande;
  montant_total: number;

  /** Renseignée uniquement quand modeCommande = LIVRAISON. */
  adresseLivraison: string | null;

  client: ClientAuthentifie;
  clientNonAuthentifie: ClientNonAuthentifie;

  table: TableRestaurant;
  employee: Employee;
}
