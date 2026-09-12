package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.UtilisateurDTO;
import org.sid.restaurationbackend.exceptions.UtilisateurNotFoundException;

import java.util.List;

public interface UtilisateurService {
    UtilisateurDTO getUtilisateurById(Long id) throws UtilisateurNotFoundException;
    List<UtilisateurDTO> getAllUtilisateurs();
    UtilisateurDTO saveUtilisateur(UtilisateurDTO utilisateurDTO);
    UtilisateurDTO updateUtilisateur(Long id, UtilisateurDTO utilisateurDTO) throws UtilisateurNotFoundException;
    void deleteUtilisateur(Long id) throws UtilisateurNotFoundException;
}
