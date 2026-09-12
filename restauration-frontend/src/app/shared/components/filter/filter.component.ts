import { Component, EventEmitter, Input, Output } from '@angular/core';

export interface FilterOption {
  label: string;
  value: string;
}

@Component({
  selector: 'app-filter',
  standalone: true,
  templateUrl: './filter.component.html',
  styleUrl: './filter.component.css',
})
export class FilterComponent {
  @Input() label = 'Filtrer';

  @Input() options: FilterOption[] = [];

  @Input() value = '';

  @Output() changed = new EventEmitter<string>();

  protected change(event: Event): void {
    const select = event.target as HTMLSelectElement;

    this.changed.emit(select.value);
  }
}
