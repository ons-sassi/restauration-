import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';

import { RoleService } from '../../../../core/services/role.service';
import { RoleFonctionnaliteService } from '../../../../core/services/role-fonctionnalite.service';
import { FonctionnaliteService } from '../../../../core/services/fonctionnalite.service';
import { AuthService } from '../../../../core/services/auth.service';

import { Fonctionnalite } from '../../../../core/models/fonctionnalite.model';
import { Module } from '../../../../core/models/module.model';

interface PermissionSelection {
  fonctionnaliteId: number;
  pdv: boolean;
  backoffice: boolean;
}

@Component({
  selector: 'app-role-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './role-form.component.html',
  styleUrls: ['./role-form.component.css'],
})
export class RoleFormComponent implements OnInit {
  roleId: number | null = null;
  editMode = false;

  nomRole = '';
  description = '';

  accesPdv = false;
  accesBackoffice = false;

  fonctionnalites: Fonctionnalite[] = [];

  selections: Map<number, PermissionSelection> = new Map();

  loading = false;
  saving = false;
  errorMessage = '';
  successMessage = '';

  // Fonctionnalité "Modifier données du compte" : toujours active en
  // Back Office pour tous les rôles, quel que soit ce que l'admin
  // coche par ailleurs — n'importe quel employé doit pouvoir modifier
  // son propre compte (page "Mon compte"), ce n'est pas une
  // permission désactivable comme les autres. Voir enforceParametresCompte().
  private readonly CODE_PARAMETRES_COMPTE = 'PARAMETRES_COMPTE';

  constructor(
    private fonctionnaliteService: FonctionnaliteService,
    private roleService: RoleService,
    private roleFonctionnaliteService: RoleFonctionnaliteService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    // Sécurité en profondeur : cette page est réservée à l'administrateur.
    // (protection principale : adminGuard sur la route)
    if (!this.authService.isAdmin()) {
      this.router.navigate(['/back-office']);
      return;
    }

    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.roleId = Number(id);
      this.editMode = true;
    }

    this.loadFonctionnalites();
  }

  /**
   * Chargement de toutes les fonctionnalités.
   */
  loadFonctionnalites(): void {
    this.loading = true;
    this.errorMessage = '';

    this.fonctionnaliteService.getAll().subscribe({
      next: (data: Fonctionnalite[]) => {
        this.fonctionnalites = data.sort(
          (a, b) => (a.ordre_affichage ?? 0) - (b.ordre_affichage ?? 0),
        );

        this.initializeSelections();

        if (this.editMode && this.roleId) {
          this.loadRolePermissions(this.roleId);
        } else {
          this.loading = false;
        }
      },

      error: (error) => {
        console.error(error);
        this.errorMessage = 'Impossible de charger les fonctionnalités.';
        this.loading = false;
      },
    });
  }

  /**
   * Initialise toutes les cases à décochées — sauf "Modifier données
   * du compte", toujours cochée par défaut en Back Office (voir
   * CODE_PARAMETRES_COMPTE).
   */
  initializeSelections(): void {
    this.fonctionnalites.forEach((f) => {
      if (!f.id_fonctionnalite) {
        return;
      }

      this.selections.set(f.id_fonctionnalite, {
        fonctionnaliteId: f.id_fonctionnalite,
        pdv: false,
        backoffice: f.codeFonctionnalite === this.CODE_PARAMETRES_COMPTE,
      });
    });
  }

  /**
   * Chargement du rôle en mode modification.
   *
   * Cette méthode peut être adaptée selon la réponse
   * exacte de ton endpoint backend.
   */
  loadRolePermissions(id: number): void {
    this.roleService.getById(id).subscribe({
      next: (role: any) => {
        this.nomRole = role.nom_role ?? role.nomRole ?? '';

        this.description = role.description ?? '';

        this.accesPdv = role.acces_pdv ?? false;

        this.accesBackoffice = role.acces_backoffice ?? false;

        // Les permissions ne font pas partie du RoleDTO renvoyé par
        // /api/roles/{id} : on va les chercher séparément via
        // RoleFonctionnaliteService (POST /api/roleFonctionnalites/by-role).
        this.roleFonctionnaliteService.getByRole({ id_role: id } as any).subscribe({
          next: (permissions: any[]) => {
            (permissions ?? []).forEach((permission: any) => {
              const fonctionnaliteId = permission.fonctionnalite?.id_fonctionnalite;

              if (!fonctionnaliteId) {
                return;
              }

              const selection = this.selections.get(fonctionnaliteId);

              if (!selection) {
                return;
              }

              if (permission.autorise !== true) {
                return;
              }

              if (permission.interfaceType === 'PDV') {
                selection.pdv = true;
              }

              if (permission.interfaceType === 'BACKOFFICE') {
                selection.backoffice = true;
              }
            });

            this.enforceParametresCompte();

            this.loading = false;
          },

          error: (error) => {
            console.error(error);

            this.errorMessage = 'Impossible de charger les permissions du rôle.';

            this.loading = false;
          },
        });
      },

      error: (error) => {
        console.error(error);

        this.errorMessage = 'Impossible de charger le rôle.';

        this.loading = false;
      },
    });
  }

  /**
   * Re-force "Modifier données du compte" à true en Back Office,
   * quoi que dise la base (ex : rôle créé avant l'introduction de
   * cette règle). À appeler après tout (re)chargement des permissions.
   */
  enforceParametresCompte(): void {
    const parametresCompte = this.fonctionnalites.find(
      (f) => f.codeFonctionnalite === this.CODE_PARAMETRES_COMPTE,
    );

    if (!parametresCompte?.id_fonctionnalite) {
      return;
    }

    const selection = this.selections.get(parametresCompte.id_fonctionnalite);

    if (selection) {
      selection.backoffice = true;
    }
  }

  /**
   * "Modifier données du compte" n'est pas une permission
   * désactivable comme les autres : tout employé doit pouvoir
   * modifier son propre compte. Sa case Back Office reste donc
   * toujours cochée et non modifiable par l'admin.
   */
  isLocked(fonctionnalite: Fonctionnalite): boolean {
    return fonctionnalite.codeFonctionnalite === this.CODE_PARAMETRES_COMPTE;
  }

  /**
   * Vérifie si une fonctionnalité est sélectionnée en PDV.
   */
  isPdvChecked(fonctionnalite: Fonctionnalite): boolean {
    if (!fonctionnalite.id_fonctionnalite) {
      return false;
    }

    return this.selections.get(fonctionnalite.id_fonctionnalite)?.pdv ?? false;
  }

  /**
   * Vérifie si une fonctionnalité est sélectionnée
   * en Back Office.
   */
  isBackofficeChecked(fonctionnalite: Fonctionnalite): boolean {
    if (!fonctionnalite.id_fonctionnalite) {
      return false;
    }

    return this.selections.get(fonctionnalite.id_fonctionnalite)?.backoffice ?? false;
  }

  /**
   * Modification de la case PDV.
   *
   * Règle métier :
   * PDV coché => Back Office automatiquement coché.
   */
  togglePdv(fonctionnalite: Fonctionnalite): void {
    if (!fonctionnalite.id_fonctionnalite) {
      return;
    }

    const selection = this.selections.get(fonctionnalite.id_fonctionnalite);

    if (!selection) {
      return;
    }

    selection.pdv = !selection.pdv;

    if (selection.pdv) {
      selection.backoffice = true;
      this.accesPdv = true;
      this.accesBackoffice = true;
    }
  }

  /**
   * Modification de la case Back Office.
   *
   * Si PDV est actif, on interdit de retirer
   * la permission Back Office.
   */
  toggleBackoffice(fonctionnalite: Fonctionnalite): void {
    if (!fonctionnalite.id_fonctionnalite) {
      return;
    }

    // Case verrouillée : "Modifier données du compte" reste toujours
    // cochée en Back Office, quoi que fasse l'admin.
    if (this.isLocked(fonctionnalite)) {
      return;
    }

    const selection = this.selections.get(fonctionnalite.id_fonctionnalite);

    if (!selection) {
      return;
    }

    if (selection.pdv) {
      selection.backoffice = true;
      return;
    }

    selection.backoffice = !selection.backoffice;

    if (selection.backoffice) {
      this.accesBackoffice = true;
    }
  }

  /**
   * Récupère les fonctionnalités principales.
   */
  getFonctionnalitesParent(): Fonctionnalite[] {
    return this.fonctionnalites.filter((f) => !f.fonctionnaliteParent);
  }

  /**
   * Modules distincts présents parmi les fonctionnalités
   * principales, triés par ordre d'affichage — sert à regrouper
   * la liste des fonctionnalités par module (Ventes, Gestion de
   * stock, Paramètres...) au lieu d'une liste plate.
   */
  getModules(): Module[] {
    const modulesParId = new Map<number, Module>();

    this.getFonctionnalitesParent().forEach((f) => {
      if (f.module?.id_module && !modulesParId.has(f.module.id_module)) {
        modulesParId.set(f.module.id_module, f.module);
      }
    });

    return Array.from(modulesParId.values()).sort(
      (a, b) => (a.ordre_affichage ?? 0) - (b.ordre_affichage ?? 0),
    );
  }

  /**
   * Fonctionnalités principales appartenant à un module donné,
   * triées par ordre d'affichage.
   */
  getFonctionnalitesParentPourModule(module: Module): Fonctionnalite[] {
    return this.getFonctionnalitesParent()
      .filter((f) => f.module?.id_module === module.id_module)
      .sort((a, b) => (a.ordre_affichage ?? 0) - (b.ordre_affichage ?? 0));
  }

  /**
   * Récupère les enfants d'une fonctionnalité.
   */
  getChildren(parent: Fonctionnalite): Fonctionnalite[] {
    return this.fonctionnalites.filter(
      (f) => f.fonctionnaliteParent?.id_fonctionnalite === parent.id_fonctionnalite,
    );
  }

  /**
   * Vérifie si la fonctionnalité doit être affichée en PDV.
   */
  disponiblePdv(f: Fonctionnalite): boolean {
    return f.disponiblePdv === true;
  }

  /**
   * Vérifie si la fonctionnalité doit être affichée
   * en Back Office.
   */
  disponibleBackoffice(f: Fonctionnalite): boolean {
    return f.disponibleBackoffice === true;
  }

  /**
   * Création de la liste des permissions à envoyer
   * au backend.
   */
  buildPermissions(): any[] {
    const permissions: any[] = [];

    this.selections.forEach((selection) => {
      if (selection.pdv) {
        permissions.push({
          fonctionnaliteId: selection.fonctionnaliteId,
          interfaceType: 'PDV',
          autorise: true,
        });

        /*
         * Règle PDV -> Back Office.
         */
        permissions.push({
          fonctionnaliteId: selection.fonctionnaliteId,
          interfaceType: 'BACKOFFICE',
          autorise: true,
        });
      } else if (selection.backoffice) {
        permissions.push({
          fonctionnaliteId: selection.fonctionnaliteId,
          interfaceType: 'BACKOFFICE',
          autorise: true,
        });
      }
    });

    return permissions;
  }

  /**
   * Sauvegarde du rôle.
   */
  save(): void {
    this.errorMessage = '';
    this.successMessage = '';

    if (!this.nomRole.trim()) {
      this.errorMessage = 'Le nom du rôle est obligatoire.';
      return;
    }

    this.saving = true;

    const configuration = {
      nom_role: this.nomRole.trim(),

      description: this.description.trim(),

      acces_pdv: this.accesPdv,

      acces_backoffice: this.accesBackoffice,

      permissions: this.buildPermissions(),
    };

    console.log('Configuration envoyée :', configuration);

    if (this.editMode && this.roleId) {
      this.roleService.updateRoleWithPermissions(this.roleId, configuration).subscribe({
        next: () => {
          this.successMessage = 'Rôle modifié avec succès.';

          this.saving = false;

          setTimeout(() => {
            this.router.navigate(['/back-office/roles']);
          }, 800);
        },

        error: (error) => {
          console.error(error);

          this.errorMessage = error?.error?.message ?? 'Erreur lors de la modification du rôle.';

          this.saving = false;
        },
      });
    } else {
      this.roleService.createRoleWithPermissions(configuration).subscribe({
        next: () => {
          this.successMessage = 'Rôle créé avec succès.';

          this.saving = false;

          setTimeout(() => {
            this.router.navigate(['/back-office/roles']);
          }, 800);
        },

        error: (error) => {
          console.error(error);

          this.errorMessage = error?.error?.message ?? 'Erreur lors de la création du rôle.';

          this.saving = false;
        },
      });
    }
  }

  cancel(): void {
    this.router.navigate(['/back-office/roles']);
  }
}
