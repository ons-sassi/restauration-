import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  templateUrl: './empty-state.component.html',
  styleUrl: './empty-state.component.css',
})
export class EmptyStateComponent {
  @Input() title = 'Aucune donnée';

  @Input() message = 'Aucun élément à afficher.';

  @Input() buttonText = '';
}
