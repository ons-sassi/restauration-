package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.ReductionDTO;
import org.sid.restaurationbackend.entities.Produit;
import org.sid.restaurationbackend.entities.Reduction;
import org.sid.restaurationbackend.enums.ApplicationReduction;
import org.sid.restaurationbackend.enums.TypeReduction;
import org.sid.restaurationbackend.exceptions.ReductionNotFoundException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.ProduitRepository;
import org.sid.restaurationbackend.repositories.ReductionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class ReductionServiceImpl implements ReductionService {

    private RestaurantMapper dtotMapper;
    private ReductionRepository reductionRepository;
    private ProduitRepository produitRepository;

    @Override
    public ReductionDTO saveReduction(ReductionDTO reductionDTO) {
        Reduction reduction = dtotMapper.fromReductionDTO(reductionDTO);

        // Une création doit toujours produire un INSERT. Si l'id n'est pas
        // remis à null ici, un id envoyé par erreur par le front (ex: 0,
        // valeur par défaut d'un formulaire) fait croire à Hibernate qu'il
        // s'agit d'une ligne existante à mettre à jour, ce qui provoque
        // l'erreur "Row was updated or deleted by another transaction"
        // puisqu'aucune ligne avec cet id n'existe réellement.
        reduction.setId_reduction(null);

        appliquerProduitsConcernes(reductionDTO, reduction);

        Reduction savedReduction = reductionRepository.save(reduction);
        return dtotMapper.fromReduction(savedReduction);
    }

    @Override
    public ReductionDTO updateReduction(Long id, ReductionDTO reductionDTO) throws ReductionNotFoundException {
        Reduction reduction = reductionRepository.findById(id)
                .orElseThrow(() -> new ReductionNotFoundException("Reduction not found"));

        dtotMapper.updateReductionFromDto(reductionDTO, reduction);

        appliquerProduitsConcernes(reductionDTO, reduction);

        Reduction updatedReduction = reductionRepository.save(reduction);
        return dtotMapper.fromReduction(updatedReduction);
    }

    /**
     * Résout et affecte les produits concernés par la réduction :
     * - TOUS_PRODUITS  : la liste de produits est vidée (elle n'est pas
     *                    utilisée, la réduction s'applique à tout le menu).
     * - PRODUITS_SPECIFIQUES : la liste est remplacée par les produits
     *                    correspondant aux ids envoyés par le front.
     */
    private void appliquerProduitsConcernes(ReductionDTO dto, Reduction entity) {

        if (entity.getApplicationProduits() == ApplicationReduction.PRODUITS_SPECIFIQUES
                && dto.getProduitsIds() != null
                && !dto.getProduitsIds().isEmpty()) {

            List<Produit> produits = produitRepository.findAllById(dto.getProduitsIds());
            entity.setProduits(produits);
        } else {
            entity.setProduits(new ArrayList<>());
        }
    }

    @Override
    public void deleteReduction(Long id) throws ReductionNotFoundException {
        Reduction reduction = reductionRepository.findById(id)
                .orElseThrow(() -> new ReductionNotFoundException("Reduction not found"));
        reductionRepository.deleteById(id);
    }

    @Override
    public ReductionDTO getReduction(Long id) throws ReductionNotFoundException {
        Reduction reduction = reductionRepository.findById(id)
                .orElseThrow(() -> new ReductionNotFoundException("Reduction not found"));
        return dtotMapper.fromReduction(reduction);
    }

    @Override
    public List<ReductionDTO> getAllReductions() {
        return reductionRepository.findAll().stream()
                .map(dtotMapper::fromReduction)
                .toList();
    }

    @Override
    public List<ReductionDTO> getReductionsByType(TypeReduction type) {
        return reductionRepository.findByType(type)
                .stream()
                .map(dtotMapper::fromReduction)
                .toList();
    }

    @Override
    public List<ReductionDTO> getReductionsBetweenDates(
            Date dateDebut,
            Date dateFin) {

        return reductionRepository
                .findByDateDebutBetween(dateDebut, dateFin)
                .stream()
                .map(dtotMapper::fromReduction)
                .toList();
    }
    @Override
    public List<ReductionDTO> getReductionsActives() {
        Date maintenant = new Date();

        return reductionRepository
                .findByDateDebutLessThanEqualAndDateFinGreaterThanEqual(
                        maintenant,
                        maintenant
                )
                .stream()
                .filter(this::estActive)
                .filter(this::limiteNonAtteinte)
                .map(dtotMapper::fromReduction)
                .toList();
    }

    @Override
    public ReductionDTO activerReduction(Long id)
            throws ReductionNotFoundException {

        Reduction reduction = reductionRepository.findById(id)
                .orElseThrow(() -> new ReductionNotFoundException("Reduction not found"));

        reduction.setActive(true);

        return dtotMapper.fromReduction(
                reductionRepository.save(reduction)
        );
    }

    @Override
    public ReductionDTO desactiverReduction(Long id)
            throws ReductionNotFoundException {

        Reduction reduction = reductionRepository.findById(id)
                .orElseThrow(() -> new ReductionNotFoundException("Reduction not found"));

        reduction.setActive(false);

        return dtotMapper.fromReduction(
                reductionRepository.save(reduction)
        );
    }

    /**
     * Une réduction dont le champ "active" n'a jamais été renseigné
     * (anciennes lignes en base, avant l'ajout de la fonctionnalité)
     * est considérée comme active, pour ne rien casser.
     */
    private boolean estActive(Reduction reduction) {
        return !Boolean.FALSE.equals(reduction.getActive());
    }

    /**
     * Vérifie que le nombre d'applications autorisées n'est pas déjà
     * atteint. null (ou <= 0 considéré comme "non défini") => illimité.
     */
    private boolean limiteNonAtteinte(Reduction reduction) {
        Integer limite = reduction.getNombreApplicationsAutorise();

        if (limite == null) {
            return true;
        }

        int effectuees = reduction.getNombreApplicationsEffectuees() != null
                ? reduction.getNombreApplicationsEffectuees()
                : 0;

        return effectuees < limite;
    }

    /**
     * Réductions qui doivent être appliquées automatiquement pour un
     * panier donné, sans sélection manuelle par le caissier.
     *
     * Une réduction est retenue si :
     * - elle est marquée "automatique",
     * - la date du jour est comprise entre sa date de début et de fin,
     * - le montant du panier atteint son montant minimum
     *   (aucune contrainte si montantMinimum est null ou 0),
     * - elle s'applique à tous les produits, OU au moins un produit du
     *   panier fait partie de sa liste de produits concernés.
     */
    @Override
    public List<ReductionDTO> getReductionsApplicables(
            Double montantCommande,
            List<Long> produitsIds) {

        Date maintenant = new Date();
        double montant = montantCommande != null ? montantCommande : 0.0;
        List<Long> idsPanier = produitsIds != null ? produitsIds : List.of();

        return reductionRepository
                .findByDateDebutLessThanEqualAndDateFinGreaterThanEqual(
                        maintenant,
                        maintenant
                )
                .stream()
                .filter(r -> Boolean.TRUE.equals(r.getAutomatique()))
                .filter(this::estActive)
                .filter(this::limiteNonAtteinte)
                .filter(r -> r.getMontantMinimum() == null
                        || r.getMontantMinimum() <= 0
                        || montant >= r.getMontantMinimum())
                .filter(r -> r.getApplicationProduits() != ApplicationReduction.PRODUITS_SPECIFIQUES
                        || (r.getProduits() != null
                                && r.getProduits().stream()
                                        .anyMatch(p -> idsPanier.contains(p.getId_element())))
                )
                .map(dtotMapper::fromReduction)
                .toList();
    }
}