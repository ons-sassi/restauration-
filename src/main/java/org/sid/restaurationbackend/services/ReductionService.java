package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.ReductionDTO;
import org.sid.restaurationbackend.enums.TypeReduction;
import org.sid.restaurationbackend.exceptions.ReductionNotFoundException;

import java.util.Date;
import java.util.List;

public interface ReductionService {
    ReductionDTO saveReduction(ReductionDTO reductionDTO);
    ReductionDTO updateReduction(Long id , ReductionDTO reductionDTO) throws ReductionNotFoundException;
    void deleteReduction(Long id) throws ReductionNotFoundException;
    ReductionDTO getReduction(Long id) throws ReductionNotFoundException;
    List<ReductionDTO> getAllReductions();

    List<ReductionDTO> getReductionsByType(TypeReduction type);

    List<ReductionDTO> getReductionsBetweenDates(
            Date dateDebut,
            Date dateFin);

    List<ReductionDTO> getReductionsActives();

    /**
     * Active manuellement une réduction (bouton "Activer").
     */
    ReductionDTO activerReduction(Long id) throws ReductionNotFoundException;

    /**
     * Désactive manuellement une réduction (bouton "Désactiver").
     * Une réduction désactivée n'est plus jamais appliquée, même si
     * ses dates de validité sont respectées.
     */
    ReductionDTO desactiverReduction(Long id) throws ReductionNotFoundException;

    /**
     * Retourne les réductions à appliquer automatiquement pour un panier
     * donné (montant total + produits présents).
     */
    List<ReductionDTO> getReductionsApplicables(
            Double montantCommande,
            List<Long> produitsIds);
}
