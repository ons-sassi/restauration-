import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-header',
  standalone: true,
  templateUrl: './header.component.html',
  styleUrl: './header.component.css',
})
export class HeaderComponent {
  @Input() title = '';

  @Input() description = '';

  @Input() breadcrumb: string[] = [];

  @Input() showBackButton = false;

  @Output() backClicked = new EventEmitter<void>();

  protected back(): void {
    this.backClicked.emit();
  }
}
