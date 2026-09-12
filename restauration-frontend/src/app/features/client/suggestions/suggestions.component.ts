import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { ClientSuggestionService } from '../../../core/services/client-suggestion.service';
import { ClientSuggestion } from '../../../core/models/client-suggestion.model';
import { ClientBottomNavComponent } from '../shared/client-bottom-nav.component';

/**
 * Sidebar client — Suggestions / conseils (voir
 * ClientSuggestionController / ClientSuggestionServiceImpl côté
 * backend : le SuggestionController existant est réservé au
 * back-office et casse pour un client authentifié).
 *
 * Simple : formulaire d'envoi + liste des suggestions déjà envoyées
 * avec leur statut de prise en compte.
 */
@Component({
  selector: 'app-client-suggestions',
  standalone: true,
  imports: [CommonModule, FormsModule, ClientBottomNavComponent],
  templateUrl: './suggestions.component.html',
  styleUrl: './suggestions.component.css',
})
export class ClientSuggestionsComponent implements OnInit {
  suggestions: ClientSuggestion[] = [];
  loading = false;
  errorMessage = '';

  contenu = '';
  submitting = false;
  submitError = '';
  submitSuccess = false;

  constructor(
    private readonly clientSuggestionService: ClientSuggestionService,
    private readonly router: Router,
    private readonly cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.loadSuggestions();
  }

  loadSuggestions(): void {
    this.loading = true;
    this.errorMessage = '';

    this.clientSuggestionService.getMesSuggestions().subscribe({
      next: (suggestions) => {
        this.suggestions = [...suggestions].sort(
          (a, b) => new Date(b.dateCreation).getTime() - new Date(a.dateCreation).getTime(),
        );
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.errorMessage = 'Impossible de charger vos suggestions pour le moment.';
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  get formulaireValide(): boolean {
    return this.contenu.trim().length > 0;
  }

  soumettre(): void {
    if (!this.formulaireValide || this.submitting) {
      return;
    }

    this.submitting = true;
    this.submitError = '';
    this.submitSuccess = false;

    this.clientSuggestionService.creer({ contenu: this.contenu.trim() }).subscribe({
      next: (suggestion) => {
        this.suggestions = [suggestion, ...this.suggestions];
        this.contenu = '';
        this.submitting = false;
        this.submitSuccess = true;
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.submitError =
          error?.error?.message ||
          "Impossible d'envoyer la suggestion pour le moment. Réessayez.";
        this.submitting = false;
        this.cdr.detectChanges();
      },
    });
  }

  retourMenu(): void {
    this.router.navigate(['/client']);
  }
}
