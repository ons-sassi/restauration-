import { Pipe, PipeTransform } from '@angular/core';

import { environment } from '../../../environments/environment';

/**
 * Transforme la valeur du champ "image" d'une Catégorie/Produit
 * (voir ElementMenu côté backend) en une URL directement utilisable
 * dans un [src].
 *
 * Ce champ peut contenir trois formats différents :
 * - une image encodée en base64 ("data:image/..."), ex. celles
 *   uploadées depuis categorie-form / produit-form : gardée telle
 *   quelle ;
 * - une URL déjà absolue ("http://..." ou "https://...") : gardée
 *   telle quelle ;
 * - une URL relative servie par le backend (ex.
 *   "/images/categories/pizzas.jpg", voir DataInitializer et le
 *   dossier src/main/resources/static/images) : préfixée avec
 *   l'origine du backend (environment.filesBaseUrl), pas avec la
 *   base "/api".
 */
@Pipe({
  name: 'mediaUrl',
  standalone: true,
})
export class MediaUrlPipe implements PipeTransform {
  transform(value: string | null | undefined): string | null {
    if (!value) {
      return null;
    }

    const trimmed = value.trim();

    if (!trimmed) {
      return null;
    }

    if (
      trimmed.startsWith('data:') ||
      trimmed.startsWith('http://') ||
      trimmed.startsWith('https://')
    ) {
      return trimmed;
    }

    return `${environment.filesBaseUrl}${trimmed}`;
  }
}
