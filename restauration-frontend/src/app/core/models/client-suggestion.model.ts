export interface ClientSuggestion {
  id_suggestion: number;
  contenu: string;
  dateCreation: string;
  priseEnCompte: boolean;
}

export interface ClientSuggestionRequest {
  contenu: string;
}
