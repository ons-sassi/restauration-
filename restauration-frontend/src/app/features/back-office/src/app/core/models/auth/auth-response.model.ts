import { InterfaceType } from '../enums/interface-type.enum';

export interface AuthResponse {
  token: string;

  type: string;

  userId: number;

  email: string;

  role: string;

  interfaceType: InterfaceType;

  pointDeVenteId: number | null;

  restaurantId: number | null;

  permissions: string[];
}
