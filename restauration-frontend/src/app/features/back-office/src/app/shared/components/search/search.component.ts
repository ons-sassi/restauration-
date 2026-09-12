import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-search',
  standalone: true,
  templateUrl: './search.component.html',
  styleUrl: './search.component.css',
})
export class SearchComponent {
  @Input() placeholder = 'Rechercher...';

  @Input() value = '';

  @Output() searched = new EventEmitter<string>();

  protected search(event: Event): void {
    const input = event.target as HTMLInputElement;

    this.searched.emit(input.value);
  }
}
