import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-price',
  standalone: true,
  templateUrl: './price.component.html',
  styleUrl: './price.component.css',
})
export class PriceComponent {
  @Input() value = 0;

  @Input() currency = 'DT';

  @Input() decimals = 2;

  protected get formattedPrice(): string {
    return new Intl.NumberFormat('fr-FR', {
      minimumFractionDigits: this.decimals,
      maximumFractionDigits: this.decimals,
    }).format(this.value);
  }
}
