import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-error-state',
  standalone: true,
  templateUrl: './error-state.component.html',
  styleUrl: './error-state.component.css',
})
export class ErrorStateComponent {
  @Input() title = 'Une erreur est survenue';

  @Input() message = 'Impossible de charger les données.';

  @Output() retry = new EventEmitter<void>();
}
