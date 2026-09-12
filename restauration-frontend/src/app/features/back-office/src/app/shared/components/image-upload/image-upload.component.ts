import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-image-upload',
  standalone: true,
  templateUrl: './image-upload.component.html',
  styleUrl: './image-upload.component.css',
})
export class ImageUploadComponent {
  @Input() previewUrl = '';

  @Input() label = 'Ajouter une image';

  @Output() imageSelected = new EventEmitter<File>();

  protected selectImage(event: Event): void {
    const input = event.target as HTMLInputElement;

    const file = input.files?.[0];

    if (!file) {
      return;
    }

    this.imageSelected.emit(file);
  }
}
