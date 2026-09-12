import { Component, EventEmitter, Output } from '@angular/core';

@Component({
  selector: 'app-pdv-header',
  standalone: true,
  imports: [],
  templateUrl: './pdv-header.component.html',
  styleUrl: './pdv-header.component.css',
})
export class PdvHeaderComponent {
  @Output()
  menuClicked = new EventEmitter<void>();

  openMenu(): void {
    this.menuClicked.emit();
  }
}
