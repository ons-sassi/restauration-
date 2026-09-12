import { Component, Input } from '@angular/core';
import { SpinnerComponent } from '../spinner/spinner.component';
@Component({
  selector: 'app-loading',
  standalone: true,
  imports: [SpinnerComponent],
  templateUrl: './loading.component.html',
  styleUrl: './loading.component.css',
})
export class LoadingComponent {
  @Input() message = 'Chargement...';
}
