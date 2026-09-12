
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { BackOfficeHeaderComponent } from '../back-office-header/back-office-header.component';
import { BackOfficeSidebarComponent } from '../back-office-sidebar/back-office-sidebar.component';
import { BackOfficeFooterComponent } from '../back-office-footer/back-office-footer.component';

@Component({
  selector: 'app-back-office-layout',
  standalone: true,
  imports: [
    RouterOutlet,
    BackOfficeHeaderComponent,
    BackOfficeSidebarComponent,
    BackOfficeFooterComponent,
  ],
  templateUrl: './back-office-layout.component.html',
  styleUrl: './back-office-layout.component.css',
})
export class BackOfficeLayoutComponent {
  sidebarCollapsed = false;

  toggleSidebar(): void {
    this.sidebarCollapsed = !this.sidebarCollapsed;
  }
}

