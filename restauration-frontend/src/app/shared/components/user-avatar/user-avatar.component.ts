import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-user-avatar',
  standalone: true,
  templateUrl: './user-avatar.component.html',
  styleUrl: './user-avatar.component.css',
})
export class UserAvatarComponent {
  @Input() name = '';

  @Input() imageUrl = '';

  @Input() size: 'small' | 'medium' | 'large' = 'medium';

  protected get initials(): string {
    if (!this.name) {
      return 'U';
    }

    const parts = this.name.trim().split(/\s+/);

    if (parts.length === 1) {
      return parts[0].charAt(0).toUpperCase();
    }

    return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
  }
}
