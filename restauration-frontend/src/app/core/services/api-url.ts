import { environment } from '../../../environments/environment';

export const API_URL = environment.apiUrl;
export const API_ROOT_URL = environment.apiUrl.replace(/\/api$/, '');
