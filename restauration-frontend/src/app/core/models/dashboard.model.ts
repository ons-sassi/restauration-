export interface Dashboard {
  ventesDuJour: number;

  commandesEnCours: number;

  tablesOccupees: number;

  nombreTables: number;

  nombreClients: number;

  ventesHier: number;

  commandesHier: number;

  clientsHier: number;

  ventes: VenteGraph[];

  dernieresCommandes: DashboardCommande[];
}

export interface VenteGraph {
  heure: string;

  montant: number;
}

export interface DashboardCommande {
  id: number;

  client: string;

  table: string;

  montant: number;

  statut: string;
}
