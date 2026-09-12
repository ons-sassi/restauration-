import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { PdvHeaderComponent } from '../pdv-header/pdv-header.component';

import { PdvSidebarComponent } from '../pdv-sidebar/pdv-sidebar.component';

@Component({
  selector: 'app-pdv-layout',
  standalone: true,

  imports: [RouterOutlet, PdvHeaderComponent, PdvSidebarComponent],

  templateUrl: './pdv-layout.component.html',
  styleUrl: './pdv-layout.component.css',
})
export class PdvLayoutComponent {
  sidebarCollapsed = false;

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }
}
