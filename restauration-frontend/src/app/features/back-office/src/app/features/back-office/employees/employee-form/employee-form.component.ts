import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { Employee } from '../../../../core/models/employee.model';
import { Role } from '../../../../core/models/role.model';
import { PointDeVente } from '../../../../core/models/point-de-vente.model';

import { EmployeeService } from '../../../../core/services/employee.service';
import { RoleService } from '../../../../core/services/role.service';
import { PointDeVenteService } from '../../../../core/services/point-de-vente.service';
import { AuthService } from '../../../../core/services/auth.service';

import { StatutPresence } from '../../../../core/models/enums/statut-presence.enum';
import { StatutUtilisateur } from '../../../../core/models/enums/statut-utilisateur.enum';

@Component({
  selector: 'app-employee-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './employee-form.component.html',
  styleUrl: './employee-form.component.css',
})
export class EmployeeFormComponent implements OnInit {
  isEditMode = false;
  employeeId: number | null = null;

  loading = false;
  saving = false;

  errorMessage = '';
  successMessage = '';

  roles: Role[] = [];
  pointsDeVente: PointDeVente[] = [];

  statutsPresence = Object.values(StatutPresence);
  statutsCompte = Object.values(StatutUtilisateur);

  /**
   * Mot de passe / code PIN déjà enregistrés (potentiellement encodés).
   *
   * En mode modification, si l'utilisateur laisse ces champs vides,
   * on renvoie ces valeurs telles quelles au backend pour éviter
   * d'écraser le mot de passe / le code PIN existant.
   */
  private motDePasseActuel = '';
  private codePinActuel = '';

  nouveauMotDePasse = '';
  nouveauCodePin = '';

  employee: Employee = {
    id_utilisateur: 0,
    nom: '',
    prenom: '',
    email: '',
    telephone: '',
    mot_de_passe: '',
    date_creation: '',
    date_derniere_connection: '',
    statut: StatutUtilisateur.ACTIF,
    photo_profil: '',

    matricule: '',
    date_embauche: '',
    codePin: '',
    salaire_base: 0,
    statutPresence: StatutPresence.ABSENT,

    role: null as unknown as Role,
    pdvAffecte: null as unknown as PointDeVente,
  };

  /**
   * Restaurant du compte (non modifiable depuis ce formulaire).
   */
  restaurant: any = null;

  constructor(
    private readonly employeeService: EmployeeService,
    private readonly roleService: RoleService,
    private readonly pointDeVenteService: PointDeVenteService,
    private readonly authService: AuthService,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  // =============================================================
  // INIT
  // =============================================================

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      const parsedId = Number(id);

      if (Number.isNaN(parsedId)) {
        this.router.navigate(['/back-office/employees']);
        return;
      }

      this.employeeId = parsedId;
      this.isEditMode = true;

      if (!this.canEditEmployee()) {
        this.router.navigate(['/back-office/employees']);
        return;
      }
    } else if (!this.canAddEmployee()) {
      this.router.navigate(['/back-office/employees']);
      return;
    }

    this.restaurant = this.authService.getSelectedRestaurant();

    this.loadRoles();
    this.loadPointsDeVente();

    if (this.isEditMode && this.employeeId) {
      this.loadEmployee(this.employeeId);
    }
  }

  // =============================================================
  // PERMISSIONS
  // =============================================================

  canAddEmployee(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('EMPLOYES_AJOUTER');
  }

  canEditEmployee(): boolean {
    return this.authService.isAdmin() || this.authService.hasPermission('EMPLOYES_MODIFIER');
  }

  canSave(): boolean {
    return this.isEditMode ? this.canEditEmployee() : this.canAddEmployee();
  }

  // =============================================================
  // ROLES (PERMISSIONS)
  // =============================================================

  loadRoles(): void {
    this.roleService.getAll().subscribe({
      next: (data) => {
        this.roles = Array.isArray(data) ? data : [];
        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erreur lors du chargement des rôles :', error);
        this.roles = [];
        this.cdr.detectChanges();
      },
    });
  }

  get selectedRoleId(): number | null {
    return this.employee.role?.id_role ?? null;
  }

  onRoleChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    const id = Number(select.value);

    if (!id) {
      this.employee.role = null as unknown as Role;
      return;
    }

    const role = this.roles.find((r) => r.id_role === id);

    if (role) {
      this.employee.role = role;
    }
  }

  // =============================================================
  // POINTS DE VENTE
  // =============================================================

  loadPointsDeVente(): void {
    const restaurantId = this.authService.getSelectedRestaurantId();

    const request$ = restaurantId
      ? this.pointDeVenteService.getByRestaurant(restaurantId)
      : this.pointDeVenteService.getAll();

    request$.subscribe({
      next: (data) => {
        this.pointsDeVente = Array.isArray(data) ? data : [];
        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erreur lors du chargement des points de vente :', error);
        this.pointsDeVente = [];
        this.cdr.detectChanges();
      },
    });
  }

  get selectedPdvId(): number | null {
    return this.employee.pdvAffecte?.id_pdv ?? null;
  }

  onPdvChange(event: Event): void {
    const select = event.target as HTMLSelectElement;
    const id = Number(select.value);

    if (!id) {
      this.employee.pdvAffecte = null as unknown as PointDeVente;
      return;
    }

    const pdv = this.pointsDeVente.find((p) => p.id_pdv === id);

    if (pdv) {
      this.employee.pdvAffecte = pdv;
    }
  }

  // =============================================================
  // CHARGEMENT (MODIFICATION)
  // =============================================================

  loadEmployee(id: number): void {
    this.loading = true;
    this.errorMessage = '';

    this.employeeService.getById(id).subscribe({
      next: (data) => {
        this.employee = { ...data };

        this.motDePasseActuel = data.mot_de_passe ?? '';
        this.codePinActuel = data.codePin ?? '';

        this.restaurant = (data as any).restaurant ?? this.restaurant;

        // On ne pré-remplit jamais les champs de saisie du mot de
        // passe / code PIN : ils restent vides tant que l'utilisateur
        // ne souhaite pas les changer.
        this.nouveauMotDePasse = '';
        this.nouveauCodePin = '';

        this.loading = false;
        this.cdr.detectChanges();
      },

      error: (error) => {
        console.error('Erreur lors du chargement de l’employé :', error);

        this.errorMessage = 'Impossible de charger cet employé.';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  // =============================================================
  // FORMAT DATE (yyyy-MM-dd pour <input type="date">)
  // =============================================================

  get dateEmbaucheInput(): string {
    if (!this.employee.date_embauche) {
      return '';
    }

    const date = new Date(this.employee.date_embauche);

    if (Number.isNaN(date.getTime())) {
      return typeof this.employee.date_embauche === 'string' ? this.employee.date_embauche : '';
    }

    return date.toISOString().substring(0, 10);
  }

  set dateEmbaucheInput(value: string) {
    this.employee.date_embauche = value;
  }

  // =============================================================
  // SAVE
  // =============================================================

  save(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.canSave()) {
      this.errorMessage = this.isEditMode
        ? 'Vous n’avez pas la permission de modifier un employé.'
        : 'Vous n’avez pas la permission de créer un employé.';

      return;
    }

    // -------------------------------------------------------
    // VALIDATION
    // -------------------------------------------------------

    if (!this.employee.nom?.trim()) {
      this.errorMessage = 'Le nom est obligatoire.';
      return;
    }

    if (!this.employee.prenom?.trim()) {
      this.errorMessage = 'Le prénom est obligatoire.';
      return;
    }

    if (!this.employee.email?.trim()) {
      this.errorMessage = 'L’email est obligatoire.';
      return;
    }

    if (!this.employee.matricule?.trim()) {
      this.errorMessage = 'Le matricule est obligatoire.';
      return;
    }

    if (!this.isEditMode && !this.nouveauMotDePasse.trim()) {
      this.errorMessage = 'Le mot de passe est obligatoire à la création.';
      return;
    }

    if (!this.employee.role || !this.employee.role.id_role) {
      this.errorMessage = 'Veuillez sélectionner un rôle (permissions) pour cet employé.';
      return;
    }

    const restaurantId = this.restaurant?.id_restaurant ?? this.authService.getSelectedRestaurantId();

    if (!this.isEditMode && !restaurantId) {
      this.errorMessage = 'Aucun restaurant sélectionné.';
      return;
    }

    // -------------------------------------------------------
    // NORMALISATION
    // -------------------------------------------------------

    this.employee.nom = this.employee.nom.trim();
    this.employee.prenom = this.employee.prenom.trim();
    this.employee.email = this.employee.email.trim();
    this.employee.telephone = this.employee.telephone?.trim() ?? '';
    this.employee.matricule = this.employee.matricule.trim();

    // -------------------------------------------------------
    // MOT DE PASSE / CODE PIN
    //
    // On ne remplace ces valeurs que si l'utilisateur a saisi
    // une nouvelle valeur. Sinon on renvoie la valeur existante
    // afin de ne pas l'écraser côté backend.
    // -------------------------------------------------------

    const motDePasse = this.nouveauMotDePasse.trim() || this.motDePasseActuel;
    const codePin = this.nouveauCodePin.trim() || this.codePinActuel;

    // -------------------------------------------------------
    // PAYLOAD
    // -------------------------------------------------------

    const payload: any = {
      nom: this.employee.nom,
      prenom: this.employee.prenom,
      email: this.employee.email,
      telephone: this.employee.telephone,
      mot_de_passe: motDePasse,
      statut: this.employee.statut,
      photo_profil: this.employee.photo_profil || null,

      matricule: this.employee.matricule,
      date_embauche: this.employee.date_embauche || null,
      codePin: codePin,
      salaire_base: Number(this.employee.salaire_base || 0),
      statutPresence: this.employee.statutPresence,

      role: this.employee.role,
      pdvAffecte: this.employee.pdvAffecte || null,

      restaurant: this.isEditMode ? this.restaurant : { id_restaurant: restaurantId },
    };

    this.saving = true;

    // =========================================================
    // UPDATE
    // =========================================================

    if (this.isEditMode && this.employeeId !== null) {
      if (!this.canEditEmployee()) {
        this.saving = false;
        this.errorMessage = 'Vous n’avez pas la permission de modifier un employé.';
        return;
      }

      this.employeeService.update(this.employeeId, payload).subscribe({
        next: () => {
          this.saving = false;
          this.successMessage = 'Employé modifié avec succès.';
          this.cdr.detectChanges();

          setTimeout(() => {
            this.router.navigate(['/back-office/employees']);
          }, 600);
        },

        error: (error) => {
          console.error('Erreur lors de la modification :', error);

          this.saving = false;
          this.errorMessage = error?.error?.message || 'Impossible de modifier cet employé.';
          this.cdr.detectChanges();
        },
      });

      return;
    }

    // =========================================================
    // CREATE
    // =========================================================

    if (!this.canAddEmployee()) {
      this.saving = false;
      this.errorMessage = 'Vous n’avez pas la permission de créer un employé.';
      return;
    }

    this.employeeService.create(payload).subscribe({
      next: () => {
        this.saving = false;
        this.successMessage = 'Employé créé avec succès.';
        this.cdr.detectChanges();

        setTimeout(() => {
          this.router.navigate(['/back-office/employees']);
        }, 600);
      },

      error: (error) => {
        console.error('Erreur lors de la création :', error);

        this.saving = false;
        this.errorMessage = error?.error?.message || 'Impossible de créer cet employé.';
        this.cdr.detectChanges();
      },
    });
  }

  // =============================================================
  // ANNULER
  // =============================================================

  cancel(): void {
    this.router.navigate(['/back-office/employees']);
  }
}
