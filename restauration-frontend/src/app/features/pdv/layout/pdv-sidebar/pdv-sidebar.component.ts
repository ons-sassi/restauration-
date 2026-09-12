import { Component, Input } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-pdv-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './pdv-sidebar.component.html',
  styleUrl: './pdv-sidebar.component.css',
})
export class PdvSidebarComponent {
  @Input()
  collapsed = false;
}
