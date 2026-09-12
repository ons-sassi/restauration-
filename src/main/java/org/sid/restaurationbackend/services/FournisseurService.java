package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.entities.Ingredient;
import org.sid.restaurationbackend.exceptions.EmployeeNotFoundException;
import org.sid.restaurationbackend.exceptions.FournisseurNotFoundException;

import java.util.List;

public interface FournisseurService {
    FournisseurDTO saveFournisseur (FournisseurDTO fournisseurDTO);
    FournisseurDTO updateFournisseur (Long id, FournisseurDTO fournisseurDTO) throws FournisseurNotFoundException;
    FournisseurDTO getFournisseur (Long id_fournisseur) throws FournisseurNotFoundException;
    void deleteFournisseur (Long id_fournisseur) throws FournisseurNotFoundException;
    List<FournisseurDTO> listFournisseurs ();
    List<FournisseurDTO> searchFournisseurs(String nom, String adresse, String email, String num);
}
