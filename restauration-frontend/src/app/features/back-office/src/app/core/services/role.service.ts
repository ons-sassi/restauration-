
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { Role } from '../models/role.model';

@Injectable({
  providedIn: 'root',
})
export class RoleService {

  private readonly API_URL = `${environment.apiUrl}/roles`;

constructor(private http: HttpClient) {}

create(role: Role): Observable<Role> {
  return this.http.post<Role>(
    this.API_URL,
    role
  );
}

update(id: number, role: Role): Observable<Role> {
  return this.http.put<Role>(
    `${this.API_URL}/${id}`,
    role
  );
}

delete(id: number): Observable<void> {
  return this.http.delete<void>(
    `${this.API_URL}/${id}`
  );
}

getById(id: number): Observable<Role> {
  return this.http.get<Role>(
    `${this.API_URL}/${id}`
  );
}

getAll(): Observable<Role[]> {
  return this.http.get<Role[]>(
    this.API_URL
  );
}

getByAttribuePar(employee: any): Observable<Role[]> {
  return this.http.post<Role[]>(
    `${this.API_URL}/by-attribue-par`,
    employee
  );
}

/**
 * Créer un rôle avec ses permissions.
 *
 * POST /roles/configure
 */
createRoleWithPermissions(
  configuration: any
): Observable<Role> {

  return this.http.post<Role>(
    `${this.API_URL}/configure`,
    configuration
  );
}

/**
 * Modifier un rôle avec ses permissions.
 *
 * PUT /roles/{id}/configure
 */
updateRoleWithPermissions(
  id: number,
  configuration: any
): Observable<Role> {

  return this.http.put<Role>(
    `${this.API_URL}/${id}/configure`,
    configuration
  );
}
}

