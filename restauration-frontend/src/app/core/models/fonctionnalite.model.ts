import { Module } from './module.model';

export interface Fonctionnalite {
  id_fonctionnalite: number;
  nomFonctionnalite: string;
  codeFonctionnalite: string;
  disponiblePdv: boolean;
  disponibleBackoffice: boolean;
  ordre_affichage: number;

  module: Module;
  fonctionnaliteParent: Fonctionnalite;
}
