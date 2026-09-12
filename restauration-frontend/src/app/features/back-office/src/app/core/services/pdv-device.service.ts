import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class PdvDeviceService {
  private readonly PDV_ID_KEY = 'pdvDeviceId';

  setPdvId(id: number): void {
    localStorage.setItem(this.PDV_ID_KEY, String(id));
  }

  getPdvId(): number | null {
    const value = localStorage.getItem(this.PDV_ID_KEY);

    if (!value) {
      return null;
    }

    const id = Number(value);

    return Number.isFinite(id) ? id : null;
  }

  isConfigured(): boolean {
    return this.getPdvId() !== null;
  }

  clear(): void {
    localStorage.removeItem(this.PDV_ID_KEY);
  }
}
