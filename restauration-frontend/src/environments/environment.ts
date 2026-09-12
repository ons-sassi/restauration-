export const environment = {
  production: false,
  apiUrl: 'http://localhost:8085/api',
  // Origine du backend SANS le suffixe "/api", utilisee pour
  // prefixer les URLs relatives renvoyees par le backend (ex.
  // photo_profil = "/uploads/photos-profil/xxx.jpg").
  filesBaseUrl: 'http://localhost:8085'
};
