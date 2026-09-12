import { Component } from '@angular/core';

@Component({
  selector: 'app-back-office-footer',
  standalone: true,
  templateUrl: './back-office-footer.component.html',
  styleUrl: './back-office-footer.component.css',
})
export class BackOfficeFooterComponent {
  readonly year = new Date().getFullYear();

  readonly version = '1.0.0';
}
