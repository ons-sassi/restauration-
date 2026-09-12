import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-footer',
  standalone: true,
  templateUrl: './footer.component.html',
  styleUrl: './footer.component.css',
})
export class FooterComponent {
  @Input() applicationName = 'Restaurant Management';

  @Input() version = '1.0.0';

  protected readonly year = new Date().getFullYear();
}
