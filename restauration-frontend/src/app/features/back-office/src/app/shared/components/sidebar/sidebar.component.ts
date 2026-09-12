import { Component, EventEmitter, Input, Output } from '@angular/core';

import { RouterLink, RouterLinkActive } from '@angular/router';

export interface SidebarItem {
  label: string;
  route: string;
  icon?: string;
  permission?: string;
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css',
})
export class SidebarComponent {
  @Input() title = 'Restaurant';
  @Input() subtitle = '';

  @Input() items: SidebarItem[] = [];

  @Input() collapsed = false;

  @Output() collapsedChange = new EventEmitter<boolean>();

  @Output() itemClicked = new EventEmitter<SidebarItem>();

  protected toggle(): void {
    this.collapsed = !this.collapsed;

    this.collapsedChange.emit(this.collapsed);
  }

  protected selectItem(item: SidebarItem): void {
    this.itemClicked.emit(item);
  }

  protected get initials(): string {
    if (!this.title) {
      return 'R';
    }

    return this.title.charAt(0).toUpperCase();
  }
}
