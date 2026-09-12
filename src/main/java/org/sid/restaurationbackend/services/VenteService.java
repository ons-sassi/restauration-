package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.entities.Recu;
import org.sid.restaurationbackend.exceptions.RecuNotFoundException;
import org.sid.restaurationbackend.exceptions.ReductionNotFoundException;
import org.sid.restaurationbackend.exceptions.VenteNotFoundException;
import org.springframework.transaction.annotation.Transactional;


import java.util.Date;
import java.util.List;

public interface VenteService {
    VenteDTO saveVente(VenteDTO venteDTO) throws ReductionNotFoundException;
    VenteDTO updateVente(Long id, VenteDTO venteDTO) throws VenteNotFoundException, ReductionNotFoundException;
    void deleteVente(Long id) throws VenteNotFoundException;
    VenteDTO getVente(Long id) throws VenteNotFoundException;
    List<VenteDTO> getAllVentes();

    @Transactional(readOnly = true)
    List<VenteDTO> searchVentes(
            PointDeVenteDTO pointDeVente,
            EmployeeDTO employee,
            ModePaiementDTO modePaiement,
            Date dateDebut,
            Date dateFin,
            Double montantHtMin,
            Double montantHtMax,
            Double montantTtcMin,
            Double montantTtcMax);

    List<VenteDTO> getVentesByCommande(CommandeDTO commande);
    List<VenteDTO> getVentesByPointDeVente(PointDeVenteDTO pointDeVente);
    List<VenteDTO> getVentesByEmployee(EmployeeDTO employee);
    List<VenteDTO> getVentesByModePaiement(ModePaiementDTO modePaiement);
    VenteDTO getVenteByRecu(RecuDTO recuDTO) throws VenteNotFoundException;

    @Transactional(readOnly = true)
    List<VenteDTO> getVentesByDate(
            Date dateDebut,
            Date dateFin);

    @Transactional(readOnly = true)
    List<VenteDTO> getVentesByPointDeVenteAndDate(
            PointDeVenteDTO pointDeVente,
            Date dateDebut,
            Date dateFin);

    RecuDTO saveRecu(RecuDTO recuDTO);
    RecuDTO updateRecu(Long id, RecuDTO recuDTO) throws RecuNotFoundException;
    void deleteRecu(Long id) throws RecuNotFoundException;
    RecuDTO getRecu(Long id) throws RecuNotFoundException;
    List<RecuDTO> getAllRecus();
    RecuDTO getRecusByVente(VenteDTO venteDTO) throws RecuNotFoundException;
    List<RecuDTO> getRecusByModelRecu(ModeleRecuDTO modelRecu);
    List<VenteDTO> getVentesRecapitulatif(
            String emailUtilisateur,
            Long pointDeVenteId,
            Date dateDebut,
            Date dateFin);

    VenteRecapitulatifDTO getRecapitulatifVentes(
            String emailUtilisateur,
            Long pointDeVenteId,
            Date dateDebut,
            Date dateFin,
            String periode
    );
    List<VenteParArticleDTO> getVentesParArticle(
            String emailUtilisateur,
            Long pointDeVenteId,
            Date dateDebut,
            Date dateFin
    );

    List<VenteParCategorieDTO> getVentesParCategorie(
            String emailUtilisateur,
            Long pointDeVenteId,
            Date dateDebut,
            Date dateFin
    );

    List<VenteParModePaiementDTO> getVentesParModePaiement(
            String emailUtilisateur,
            Long pointDeVenteId,
            Date dateDebut,
            Date dateFin
    );

    List<VenteParRecuDTO> getVentesParRecu(
            String emailUtilisateur,
            Long pointDeVenteId,
            Date dateDebut,
            Date dateFin
    );

    List<VenteParEmployeeDTO> getVentesParEmployee(
            String emailUtilisateur,
            Long pointDeVenteId,
            Date dateDebut,
            Date dateFin
    );

    List<VenteParModificateurDTO> getVentesParModificateur(
            String emailUtilisateur,
            Long pointDeVenteId,
            Date dateDebut,
            Date dateFin
    );

    List<VenteParReductionDTO> getVentesParReduction(
            String emailUtilisateur,
            Long pointDeVenteId,
            Date dateDebut,
            Date dateFin
    );

}