import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-modal',
  standalone: true,
  templateUrl: './modal.component.html',
  styleUrl: './modal.component.css',
})
export class ModalComponent {
  @Input() opened = false;

  @Input() title = '';

  @Input() width = '500px';

  @Input() closeOnBackdrop = true;

  @Output() closed = new EventEmitter<void>();

  protected close(): void {
    this.closed.emit();
  }

  protected onBackdropClick(): void {
    if (this.closeOnBackdrop) {
      this.close();
    }
  }
}
