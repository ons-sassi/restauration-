import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-navbar',
  standalone: true,
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css'
})
export class NavbarComponent {

  @Input() userName = '';
  @Input() userRole = '';
  @Input() userImage = '';

  @Input() searchPlaceholder = 'Rechercher...';

  @Input() notificationCount = 0;

  @Output() menuClicked = new EventEmitter<void>();
  @Output() searchChanged = new EventEmitter<string>();
  @Output() notificationClicked = new EventEmitter<void>();
  @Output() profileClicked = new EventEmitter<void>();


  protected onSearch(event: Event): void {
    const input = event.target as HTMLInputElement;

    this.searchChanged.emit(input.value);
  }

  protected onMenuClick(): void {
    this.menuClicked.emit();
  }

  protected onNotificationClick(): void {
    this.notificationClicked.emit();
  }

  protected onProfileClick(): void {
    this.profileClicked.emit();
  }

  protected get initials(): string {

    if (!this.userName) {
      return 'U';
    }

    const parts = this.userName
      .trim()
      .split(/\s+/);

    if (parts.length === 1) {
      return parts[0].charAt(0).toUpperCase();
    }

    return (
      parts[0].charAt(0) +
      parts[parts.length - 1].charAt(0)
    ).toUpperCase();
  }
}
