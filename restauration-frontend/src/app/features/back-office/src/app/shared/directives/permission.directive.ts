import { Directive, Input, TemplateRef, ViewContainerRef, inject } from '@angular/core';

import { AuthService } from '../../core/services/auth.service';

@Directive({
  selector: '[appHasPermission]',
  standalone: true,
})
export class PermissionDirective {
  private readonly templateRef = inject(TemplateRef<unknown>);

  private readonly viewContainer = inject(ViewContainerRef);

  private readonly authService = inject(AuthService);

  @Input()
  set appHasPermission(permission: string) {
    this.viewContainer.clear();

    if (!permission) {
      return;
    }

    if (this.authService.hasPermission(permission)) {
      this.viewContainer.createEmbeddedView(this.templateRef);
    }
  }
}
