import { TypeReduction, ApplicationReduction } from './enums/type-reduction.enum';
import { Restaurant } from './restaurant.model';

export interface ReductionProduit {
  id: number;
  nom: string;
}

export interface Reduction {
  id_reduction: number;
  nom_reduction: string;
  type: TypeReduction;
  valeur: number;

  // Requis par le backend à la création (SUPERADMIN : restaurant
  // explicite obligatoire ; employé : doit correspondre à son propre
  // restaurant). Voir ReductionController.saveReduction.
  restaurant?: Restaurant;

  // Le backend sérialise ces champs en snake_case
  // (voir ReductionDTO : @JsonProperty("date_debut") / @JsonProperty("date_fin")).
  date_debut: string;
  date_fin: string;

  conditions_application: string;

  // Périmètre produits : tous les produits, ou une sélection précise.
  application_produits: ApplicationReduction;

  // Envoyé pour créer/modifier (uniquement utile si application_produits
  // = PRODUITS_SPECIFIQUES).
  produits_ids: number[];

  // Renvoyé par le backend pour affichage (id + nom des produits).
  produits?: ReductionProduit[];

  // Montant minimum de la commande pour que la réduction s'applique.
  // null (ou 0) => applicable à partir de n'importe quel montant.
  montant_minimum: number | null;

  // Si true, la réduction est appliquée automatiquement dès que ses
  // conditions sont réunies, sans sélection manuelle par le caissier.
  automatique: boolean;

  // Activation manuelle de la réduction. Si false, la réduction est
  // désactivée : elle n'est plus jamais appliquée (automatiquement ou
  // manuellement), même si ses dates de validité sont respectées.
  active: boolean;

  // Nombre maximum de fois où la réduction peut être appliquée.
  // null (ou undefined) => nombre d'applications illimité.
  nombre_applications_autorise: number | null;

  // Nombre de fois où la réduction a déjà été appliquée.
  // Renvoyé par le backend en lecture seule.
  nombre_applications_effectuees?: number;
}
