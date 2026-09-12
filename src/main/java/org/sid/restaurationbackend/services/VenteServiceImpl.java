package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.entities.*;
import org.sid.restaurationbackend.enums.ApplicationReduction;
import org.sid.restaurationbackend.enums.TypeReduction;
import org.sid.restaurationbackend.exceptions.RecuNotFoundException;
import org.sid.restaurationbackend.exceptions.ReductionNotFoundException;
import org.sid.restaurationbackend.exceptions.VenteNotFoundException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.EmployeeRepository;
import org.sid.restaurationbackend.repositories.ModificateurRepository;
import org.sid.restaurationbackend.repositories.RecuRepository;
import org.sid.restaurationbackend.repositories.ReductionRepository;
import org.sid.restaurationbackend.repositories.VenteRepository;
import org.sid.restaurationbackend.repositories.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class VenteServiceImpl implements VenteService {

    private final RestaurantMapper dtoMapper;
    private final VenteRepository venteRepository;
    private final RecuRepository recuRepository;
    private final ReductionRepository reductionRepository;
    private final EmployeeRepository employeeRepository;
    private final ModificateurRepository modificateurRepository;
    private final RestaurantRepository restaurantRepository;
    private final CurrentUserService currentUserService;


    // ============================================================
    // VENTES
    // ============================================================

    @Override
    public VenteDTO saveVente(VenteDTO venteDTO)
            throws ReductionNotFoundException {

        if (venteDTO == null) {
            throw new IllegalArgumentException(
                    "Les données de la vente sont obligatoires"
            );
        }

        Vente vente = dtoMapper.fromVenteDTO(venteDTO);

        // Date automatique
        vente.setDateVente(new Date());


        // ========================================================
        // RECUPERATION DE LA REDUCTION
        // ========================================================

        if (venteDTO.getReduction() != null
                && venteDTO.getReduction().getId_reduction() != null) {

            Long reductionId =
                    venteDTO.getReduction().getId_reduction();

            Reduction reduction =
                    reductionRepository
                            .findById(reductionId)
                            .orElseThrow(() ->
                                    new ReductionNotFoundException(
                                            "Réduction introuvable : "
                                                    + reductionId
                                    )
                            );

            vente.setReduction(reduction);

        } else {
            vente.setReduction(null);
        }


        // ========================================================
        // APPLICATION DE LA REDUCTION
        // ========================================================

        appliquerReduction(vente);


        // ========================================================
        // VALEURS PAR DEFAUT
        // ========================================================

        if (vente.getMontantReduction() == null) {
            vente.setMontantReduction(0.0);
        }

        if (vente.getMontantTtc() == null) {
            vente.setMontantTtc(0.0);
        }


        // ========================================================
        // SAUVEGARDE
        // ========================================================

        Vente savedVente =
                venteRepository.save(vente);

        // ========================================================
        // COMPTEUR D'APPLICATIONS DE LA REDUCTION
        // ========================================================
        // Incrémenté uniquement quand la réduction a effectivement été
        // appliquée (montant de réduction > 0), pour permettre de
        // limiter le nombre total d'utilisations d'une réduction.

        incrementerCompteurApplication(savedVente);

        log.info(
                "Vente créée : {} | TTC : {} | Réduction : {} | ID réduction : {}",
                savedVente.getId_vente(),
                savedVente.getMontantTtc(),
                savedVente.getMontantReduction(),
                savedVente.getReduction() != null
                        ? savedVente.getReduction().getId_reduction()
                        : null
        );

        return dtoMapper.fromVente(savedVente);
    }


    // ============================================================
    // MODIFIER UNE VENTE
    // ============================================================

    @Override
    public VenteDTO updateVente(
            Long id,
            VenteDTO venteDTO)
            throws VenteNotFoundException,
            ReductionNotFoundException {

        Vente vente =
                venteRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new VenteNotFoundException(
                                        "Vente not found : " + id
                                )
                        );

        if (venteDTO == null) {
            throw new IllegalArgumentException(
                    "Les données de la vente sont obligatoires"
            );
        }


        // ========================================================
        // MISE A JOUR
        // ========================================================

        dtoMapper.updateVenteFromDto(
                venteDTO,
                vente
        );


        // ========================================================
        // REDUCTION
        // ========================================================

        if (venteDTO.getReduction() != null
                && venteDTO.getReduction().getId_reduction() != null) {

            Long reductionId =
                    venteDTO.getReduction().getId_reduction();

            Reduction reduction =
                    reductionRepository
                            .findById(reductionId)
                            .orElseThrow(() ->
                                    new ReductionNotFoundException(
                                            "Réduction introuvable : "
                                                    + reductionId
                                    )
                            );

            vente.setReduction(reduction);

        } else {
            vente.setReduction(null);
        }


        // ========================================================
        // APPLICATION DE LA REDUCTION
        // ========================================================

        appliquerReduction(vente);


        // ========================================================
        // VALEURS PAR DEFAUT
        // ========================================================

        if (vente.getMontantReduction() == null) {
            vente.setMontantReduction(0.0);
        }

        if (vente.getMontantTtc() == null) {
            vente.setMontantTtc(0.0);
        }


        // ========================================================
        // SAUVEGARDE
        // ========================================================

        Vente updatedVente =
                venteRepository.save(vente);

        log.info(
                "Vente modifiée : {} | TTC : {} | Réduction : {}",
                updatedVente.getId_vente(),
                updatedVente.getMontantTtc(),
                updatedVente.getMontantReduction()
        );

        return dtoMapper.fromVente(updatedVente);
    }


    // ============================================================
    // COMPTEUR D'APPLICATIONS DE LA REDUCTION
    // ============================================================

    /**
     * Incrémente le compteur "nombreApplicationsEffectuees" de la
     * réduction utilisée par cette vente, si la réduction a bien été
     * appliquée (montant de réduction > 0).
     *
     * Fait uniquement à la création de la vente (voir saveVente),
     * pour éviter de compter plusieurs fois la même vente si elle est
     * ensuite modifiée.
     */
    private void incrementerCompteurApplication(Vente vente) {

        if (vente == null
                || vente.getReduction() == null
                || vente.getMontantReduction() == null
                || vente.getMontantReduction() <= 0) {
            return;
        }

        Reduction reduction = vente.getReduction();

        int applicationsEffectuees =
                reduction.getNombreApplicationsEffectuees() != null
                        ? reduction.getNombreApplicationsEffectuees()
                        : 0;

        reduction.setNombreApplicationsEffectuees(applicationsEffectuees + 1);

        reductionRepository.save(reduction);
    }


    // ============================================================
    // APPLICATION REDUCTION
    // ============================================================

    private void appliquerReduction(Vente vente) {

        if (vente == null) {
            return;
        }

        Double montantTtc =
                vente.getMontantTtc();

        if (montantTtc == null) {
            vente.setMontantReduction(0.0);
            return;
        }


        // --------------------------------------------------------
        // AUCUNE REDUCTION
        // --------------------------------------------------------

        if (vente.getReduction() == null) {
            vente.setMontantReduction(0.0);
            return;
        }


        Reduction reduction =
                vente.getReduction();


        // --------------------------------------------------------
        // REDUCTION DESACTIVEE MANUELLEMENT
        // --------------------------------------------------------
        // Une réduction dont le champ "active" n'a jamais été renseigné
        // (anciennes lignes en base) est considérée comme active.

        if (Boolean.FALSE.equals(reduction.getActive())) {
            vente.setReduction(null);
            vente.setMontantReduction(0.0);
            return;
        }


        // --------------------------------------------------------
        // NOMBRE D'APPLICATIONS AUTORISEES ATTEINT
        // --------------------------------------------------------
        // null (ou <= 0) => illimité.

        Integer limiteApplications =
                reduction.getNombreApplicationsAutorise();

        if (limiteApplications != null && limiteApplications > 0) {

            int applicationsEffectuees =
                    reduction.getNombreApplicationsEffectuees() != null
                            ? reduction.getNombreApplicationsEffectuees()
                            : 0;

            if (applicationsEffectuees >= limiteApplications) {
                vente.setReduction(null);
                vente.setMontantReduction(0.0);
                return;
            }
        }


        // --------------------------------------------------------
        // VERIFICATION
        // --------------------------------------------------------

        if (reduction.getType() == null
                || reduction.getValeur() == null) {

            vente.setMontantReduction(0.0);
            return;
        }

        // --------------------------------------------------------
        // MONTANT MINIMUM REQUIS
        // --------------------------------------------------------
        // Une réduction configurée avec un montant minimum ne doit
        // pas s'appliquer si le total de la vente ne l'atteint pas.
        // (Pas de contrainte si montantMinimum est null ou 0
        // => applicable à partir de n'importe quel montant.)

        if (reduction.getMontantMinimum() != null
                && reduction.getMontantMinimum() > 0
                && montantTtc < reduction.getMontantMinimum()) {

            vente.setMontantReduction(0.0);
            return;
        }

        // --------------------------------------------------------
        // PRODUITS CONCERNES
        // --------------------------------------------------------
        // Si la réduction ne concerne que des produits spécifiques,
        // elle ne s'applique que si la commande contient au moins
        // un de ces produits.

        if (reduction.getApplicationProduits()
                == ApplicationReduction.PRODUITS_SPECIFIQUES) {

            List<Long> produitsReduction =
                    reduction.getProduits() != null
                            ? reduction.getProduits().stream()
                            .map(Produit::getId_element)
                            .toList()
                            : List.of();

            List<LigneCommande> lignesCommande =
                    vente.getCommande() != null
                            ? vente.getCommande().getLignes()
                            : null;

            boolean produitConcerneDansPanier =
                    lignesCommande != null
                            && lignesCommande.stream()
                            .anyMatch(l -> l.getProduit() != null
                                    && produitsReduction.contains(l.getProduit().getId_element()));

            if (!produitConcerneDansPanier) {
                vente.setMontantReduction(0.0);
                return;
            }
        }


        double valeurReduction =
                reduction.getValeur();

        if (valeurReduction < 0) {
            valeurReduction = 0.0;
        }


        double montantReduction;


        // --------------------------------------------------------
        // POURCENTAGE
        // --------------------------------------------------------

        if (reduction.getType() == TypeReduction.POURCENTAGE) {

            montantReduction =
                    montantTtc
                            * valeurReduction
                            / 100.0;

        }

        // --------------------------------------------------------
        // MONTANT FIXE
        // --------------------------------------------------------

        else if (reduction.getType() == TypeReduction.MONTANT_FIXE) {

            montantReduction =
                    valeurReduction;

        }

        // --------------------------------------------------------
        // TYPE INCONNU
        // --------------------------------------------------------

        else {
            montantReduction = 0.0;
        }


        // --------------------------------------------------------
        // SECURITE
        // --------------------------------------------------------

        montantReduction =
                Math.min(
                        montantReduction,
                        montantTtc
                );

        montantReduction =
                arrondir(montantReduction);

        vente.setMontantReduction(
                montantReduction
        );


        // --------------------------------------------------------
        // MONTANT NET
        // --------------------------------------------------------

        double montantNet =
                montantTtc -
                        montantReduction;

        vente.setMontantTtc(
                arrondir(montantNet)
        );
    }


    // ============================================================
    // ARRONDI
    // ============================================================

    private double arrondir(double valeur) {

        return Math.round(
                valeur * 100.0
        ) / 100.0;
    }


    // ============================================================
    // GET VENTE
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public VenteDTO getVente(Long id)
            throws VenteNotFoundException {

        Vente vente =
                venteRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new VenteNotFoundException(
                                        "Vente not found"
                                )
                        );

        return dtoMapper.fromVente(vente);
    }


    // ============================================================
    // DELETE VENTE
    // ============================================================

    @Override
    public void deleteVente(Long id)
            throws VenteNotFoundException {

        Vente vente =
                venteRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new VenteNotFoundException(
                                        "Vente not found : " + id
                                )
                        );

        venteRepository.delete(vente);

        log.info(
                "Vente {} supprimée avec succès.",
                id
        );
    }


    // ============================================================
    // TOUTES LES VENTES
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<VenteDTO> getAllVentes() {

        return venteRepository
                .findAll()
                .stream()
                .map(dtoMapper::fromVente)
                .toList();
    }


    // ============================================================
    // RECHERCHE DES VENTES
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<VenteDTO> searchVentes(
            PointDeVenteDTO pointDeVente,
            EmployeeDTO employee,
            ModePaiementDTO modePaiement,
            Date dateDebut,
            Date dateFin,
            Double montantHtMin,
            Double montantHtMax,
            Double montantTtcMin,
            Double montantTtcMax) {

        PointDeVente pointDeVenteEntity =
                pointDeVente == null
                        ? null
                        : dtoMapper.fromPointDeVenteDTO(
                        pointDeVente
                );

        Employee employeeEntity =
                employee == null
                        ? null
                        : dtoMapper.fromEmployeeDTO(
                        employee
                );

        ModePaiement modePaiementEntity =
                modePaiement == null
                        ? null
                        : dtoMapper.fromModePaiementDTO(
                        modePaiement
                );

        return venteRepository
                .search(
                        pointDeVenteEntity,
                        employeeEntity,
                        modePaiementEntity,
                        dateDebut,
                        dateFin,
                        montantHtMin,
                        montantHtMax,
                        montantTtcMin,
                        montantTtcMax
                )
                .stream()
                .map(dtoMapper::fromVente)
                .toList();
    }


    // ============================================================
    // VENTES DU RECAPITULATIF
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<VenteDTO> getVentesRecapitulatif(
            String emailUtilisateur,
            Long pointDeVenteId,
            Date dateDebut,
            Date dateFin) {

        if (emailUtilisateur == null || emailUtilisateur.isBlank()) {
            throw new IllegalArgumentException("Utilisateur connecté introuvable");
        }

        if (dateDebut == null || dateFin == null) {
            throw new IllegalArgumentException(
                    "Les dates de début et de fin sont obligatoires"
            );
        }

        if (dateDebut.after(dateFin)) {
            throw new IllegalArgumentException(
                    "La date de début doit être antérieure ou égale à la date de fin"
            );
        }

        Restaurant restaurant = getRestaurantPourUtilisateur(emailUtilisateur);
        Long restaurantId = restaurant.getId_restaurant();

        if (restaurantId == null) {
            throw new IllegalArgumentException(
                    "Le restaurant sélectionné possède un identifiant invalide"
            );
        }

        verifierPointDeVente(restaurant, pointDeVenteId);

        Date dateFinInclusive = rendreDateFinInclusive(dateFin);

        return venteRepository
                .searchByRestaurant(
                        restaurantId,
                        pointDeVenteId,
                        dateDebut,
                        dateFinInclusive
                )
                .stream()
                .map(dtoMapper::fromVente)
                .toList();
    }


    // ============================================================
    // RECAPITULATIF COMPLET
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public VenteRecapitulatifDTO getRecapitulatifVentes(
            String emailUtilisateur,
            Long pointDeVenteId,
            Date dateDebut,
            Date dateFin,
            String periode) {

        if (emailUtilisateur == null || emailUtilisateur.isBlank()) {
            throw new IllegalArgumentException("Utilisateur connecté introuvable");
        }

        if (dateDebut == null || dateFin == null) {
            throw new IllegalArgumentException(
                    "Les dates de début et de fin sont obligatoires"
            );
        }

        if (dateDebut.after(dateFin)) {
            throw new IllegalArgumentException(
                    "La date de début doit être antérieure ou égale à la date de fin"
            );
        }

        Restaurant restaurant = getRestaurantPourUtilisateur(emailUtilisateur);

        Long restaurantId = restaurant.getId_restaurant();

        if (restaurantId == null) {

            throw new IllegalArgumentException(
                    "Le restaurant de l'employé possède un identifiant invalide"
            );
        }


        verifierPointDeVente(restaurant, pointDeVenteId);

        // ========================================================
        // DATE FIN INCLUSIVE
        // ========================================================

        Date dateFinInclusive =
                rendreDateFinInclusive(dateFin);

        // ========================================================
        // RECUPERATION DES VENTES
        //
        // Le restaurantId vient du contexte courant :
        // SUPERADMIN -> restaurant sélectionné dans le JWT
        // Employé classique -> restaurant de son compte
        // ========================================================

        List<Vente> ventes =
                venteRepository
                        .searchByRestaurant(
                                restaurantId,
                                pointDeVenteId,
                                dateDebut,
                                dateFinInclusive
                        );


        // ========================================================
        // CHIFFRE D'AFFAIRES
        //
        // montantTtc contient ici le montant net après réduction.
        // ========================================================

        double chiffreAffaires =
                ventes
                        .stream()
                        .map(Vente::getMontantTtc)
                        .filter(montant -> montant != null)
                        .mapToDouble(Double::doubleValue)
                        .sum();

        chiffreAffaires =
                arrondir(chiffreAffaires);


        // ========================================================
        // REDUCTIONS
        // ========================================================

        double reductions =
                ventes
                        .stream()
                        .map(Vente::getMontantReduction)
                        .filter(montant -> montant != null)
                        .mapToDouble(Double::doubleValue)
                        .sum();

        reductions =
                arrondir(reductions);


        // ========================================================
        // VENTE BRUTE
        //
        // Vente brute = vente nette + réductions
        // ========================================================

        double venteBrute =
                arrondir(
                        chiffreAffaires
                                + reductions
                );


        // ========================================================
        // VENTE NETTE
        // ========================================================

        double venteNette =
                arrondir(
                        venteBrute
                                - reductions
                );


        // ========================================================
        // MARGE BRUTE
        //
        // Marge brute =
        // Vente nette - coût des produits vendus
        //
        // On utilise un Set afin de ne pas compter plusieurs fois
        // le coût d'une commande si plusieurs ventes lui sont liées.
        // ========================================================

        Set<Long> commandesDejaCalculees =
                new HashSet<>();

        double coutProduitsVendus = 0.0;


        for (Vente vente : ventes) {

            if (vente.getCommande() == null) {
                continue;
            }

            Commande commande =
                    vente.getCommande();

            Long commandeId =
                    commande.getId_commande();


            // ----------------------------------------------------
            // Eviter le double comptage
            // ----------------------------------------------------

            if (commandeId != null
                    && !commandesDejaCalculees.add(commandeId)) {

                continue;
            }


            // ----------------------------------------------------
            // LIGNES DE COMMANDE
            // ----------------------------------------------------

            if (commande.getLignes() == null) {
                continue;
            }


            for (LigneCommande ligne :
                    commande.getLignes()) {

                if (ligne.getProduit() == null
                        || ligne.getProduit().getCoutUnitaire() == null
                        || ligne.getQuantite() == null) {

                    continue;
                }

                coutProduitsVendus +=
                        ligne.getQuantite()
                                * ligne.getProduit()
                                .getCoutUnitaire();
            }
        }


        coutProduitsVendus =
                arrondir(coutProduitsVendus);


        double margeBrute =
                arrondir(
                        venteNette
                                - coutProduitsVendus
                );


        // ========================================================
        // EVOLUTION DES VENTES
        // ========================================================

        String periodeNormalisee =
                normaliserPeriode(periode);


        /*
         * JOUR :
         * 0 -> 00:00
         * 1 -> 01:00
         * ...
         * 23 -> 23:00
         *
         * SEMAINE :
         * 1 -> Lundi
         * ...
         * 7 -> Dimanche
         *
         * MOIS :
         * 1 -> Jour 01
         * ...
         *
         * TRIMESTRE :
         * 1 -> T1
         * 2 -> T2
         * 3 -> T3
         * 4 -> T4
         *
         * ANNEE :
         * 1 -> Janvier
         * ...
         * 12 -> Décembre
         */

        Map<Integer, Double> ventesParPeriode =
                new TreeMap<>();


        for (Vente vente : ventes) {

            if (vente.getDateVente() == null
                    || vente.getMontantTtc() == null) {

                continue;
            }

            Calendar calendar =
                    Calendar.getInstance();

            calendar.setTime(
                    vente.getDateVente()
            );


            int cle =
                    obtenirClePeriode(
                            calendar,
                            periodeNormalisee
                    );


            ventesParPeriode.merge(
                    cle,
                    vente.getMontantTtc(),
                    Double::sum
            );
        }


        // ========================================================
        // CONSTRUCTION DU GRAPHIQUE
        // ========================================================

        List<VenteGraphDTO> evolution =
                new ArrayList<>();


        for (Map.Entry<Integer, Double> entry :
                ventesParPeriode.entrySet()) {

            String libelle =
                    obtenirLibellePeriode(
                            entry.getKey(),
                            periodeNormalisee
                    );

            evolution.add(
                    new VenteGraphDTO(
                            libelle,
                            arrondir(entry.getValue())
                    )
            );
        }


        // ========================================================
        // RESULTAT
        // ========================================================

        return new VenteRecapitulatifDTO(
                chiffreAffaires,
                venteBrute,
                reductions,
                venteNette,
                margeBrute,
                evolution
        );
    }


    // ============================================================
    // NORMALISATION PERIODE
    // ============================================================

    private String normaliserPeriode(String periode) {

        if (periode == null
                || periode.isBlank()) {

            return "JOUR";
        }

        String valeur =
                periode
                        .trim()
                        .toUpperCase();

        return switch (valeur) {

            case "JOUR", "DAY" ->
                    "JOUR";

            case "SEMAINE", "WEEK" ->
                    "SEMAINE";

            case "MOIS", "MONTH" ->
                    "MOIS";

            case "TRIMESTRE", "QUARTER" ->
                    "TRIMESTRE";

            case "ANNEE", "ANNÉE", "YEAR" ->
                    "ANNEE";

            default ->
                    throw new IllegalArgumentException(
                            "Période invalide : "
                                    + periode
                                    + ". Valeurs acceptées : JOUR, "
                                    + "SEMAINE, MOIS, TRIMESTRE, ANNEE"
                    );
        };
    }


    // ============================================================
    // CLE PERIODE
    // ============================================================

    private int obtenirClePeriode(
            Calendar calendar,
            String periode) {

        return switch (periode) {

            // ----------------------------------------------------
            // JOUR
            // ----------------------------------------------------

            case "JOUR" ->
                    calendar.get(
                            Calendar.HOUR_OF_DAY
                    );


            // ----------------------------------------------------
            // SEMAINE
            // ----------------------------------------------------

            case "SEMAINE" -> {

                int jour =
                        calendar.get(
                                Calendar.DAY_OF_WEEK
                        );

                /*
                 * Calendar :
                 *
                 * DIMANCHE = 1
                 * LUNDI    = 2
                 * ...
                 * SAMEDI   = 7
                 *
                 * Nous voulons :
                 *
                 * LUNDI    = 1
                 * ...
                 * DIMANCHE = 7
                 */

                yield jour == Calendar.SUNDAY
                        ? 7
                        : jour - 1;
            }


            // ----------------------------------------------------
            // MOIS
            // ----------------------------------------------------

            case "MOIS" ->
                    calendar.get(
                            Calendar.DAY_OF_MONTH
                    );


            // ----------------------------------------------------
            // TRIMESTRE
            // ----------------------------------------------------

            case "TRIMESTRE" -> {

                int mois =
                        calendar.get(
                                Calendar.MONTH
                        );

                /*
                 * Calendar.MONTH :
                 *
                 * Janvier = 0
                 * Février = 1
                 * ...
                 * Décembre = 11
                 *
                 * Donc :
                 *
                 * 0,1,2   -> T1
                 * 3,4,5   -> T2
                 * 6,7,8   -> T3
                 * 9,10,11 -> T4
                 */

                yield (mois / 3) + 1;
            }


            // ----------------------------------------------------
            // ANNEE
            // ----------------------------------------------------

            case "ANNEE" ->
                    calendar.get(
                            Calendar.MONTH
                    ) + 1;


            default ->
                    throw new IllegalArgumentException(
                            "Période inconnue : "
                                    + periode
                    );
        };
    }


    // ============================================================
    // LIBELLE PERIODE
    // ============================================================

    private String obtenirLibellePeriode(
            int cle,
            String periode) {

        return switch (periode) {

            // ----------------------------------------------------
            // JOUR
            // ----------------------------------------------------

            case "JOUR" ->
                    String.format(
                            "%02d:00",
                            cle
                    );


            // ----------------------------------------------------
            // SEMAINE
            // ----------------------------------------------------

            case "SEMAINE" -> {

                String[] jours = {
                        "Lundi",
                        "Mardi",
                        "Mercredi",
                        "Jeudi",
                        "Vendredi",
                        "Samedi",
                        "Dimanche"
                };

                yield jours[cle - 1];
            }


            // ----------------------------------------------------
            // MOIS
            // ----------------------------------------------------

            case "MOIS" ->
                    String.format(
                            "Jour %02d",
                            cle
                    );


            // ----------------------------------------------------
            // TRIMESTRE
            // ----------------------------------------------------

            case "TRIMESTRE" ->
                    "T" + cle;


            // ----------------------------------------------------
            // ANNEE
            // ----------------------------------------------------

            case "ANNEE" -> {

                String[] mois = {
                        "Janvier",
                        "Février",
                        "Mars",
                        "Avril",
                        "Mai",
                        "Juin",
                        "Juillet",
                        "Août",
                        "Septembre",
                        "Octobre",
                        "Novembre",
                        "Décembre"
                };

                yield mois[cle - 1];
            }


            default ->
                    throw new IllegalArgumentException(
                            "Période inconnue : "
                                    + periode
                    );
        };
    }


    // ============================================================
    // VENTES PAR COMMANDE
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<VenteDTO> getVentesByCommande(
            CommandeDTO commande) {

        return venteRepository
                .findByCommande(
                        dtoMapper.fromCommandeDTO(
                                commande
                        )
                )
                .stream()
                .map(dtoMapper::fromVente)
                .toList();
    }


    // ============================================================
    // VENTES PAR POINT DE VENTE
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<VenteDTO> getVentesByPointDeVente(
            PointDeVenteDTO pointDeVente) {

        return venteRepository
                .findByPointDeVente(
                        dtoMapper.fromPointDeVenteDTO(
                                pointDeVente
                        )
                )
                .stream()
                .map(dtoMapper::fromVente)
                .toList();
    }


    // ============================================================
    // VENTES PAR EMPLOYE
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<VenteDTO> getVentesByEmployee(
            EmployeeDTO employee) {

        return venteRepository
                .findByEmployee(
                        dtoMapper.fromEmployeeDTO(
                                employee
                        )
                )
                .stream()
                .map(dtoMapper::fromVente)
                .toList();
    }


    // ============================================================
    // VENTES PAR MODE DE PAIEMENT
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<VenteDTO> getVentesByModePaiement(
            ModePaiementDTO modePaiement) {

        return venteRepository
                .findByModePaiement(
                        dtoMapper.fromModePaiementDTO(
                                modePaiement
                        )
                )
                .stream()
                .map(dtoMapper::fromVente)
                .toList();
    }


    // ============================================================
    // VENTE PAR RECU
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public VenteDTO getVenteByRecu(
            RecuDTO recuDTO)
            throws VenteNotFoundException {

        Vente vente =
                venteRepository
                        .findByRecu(
                                dtoMapper.fromRecuDTO(
                                        recuDTO
                                )
                        )
                        .orElseThrow(() ->
                                new VenteNotFoundException(
                                        "Vente not found"
                                )
                        );

        return dtoMapper.fromVente(vente);
    }


    // ============================================================
    // VENTES PAR DATE
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<VenteDTO> getVentesByDate(
            Date dateDebut,
            Date dateFin) {

        Date dateFinInclusive =
                rendreDateFinInclusive(
                        dateFin
                );

        return venteRepository
                .findByDateVenteBetween(
                        dateDebut,
                        dateFinInclusive
                )
                .stream()
                .map(dtoMapper::fromVente)
                .toList();
    }


    // ============================================================
    // VENTES PAR PDV + DATE
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<VenteDTO> getVentesByPointDeVenteAndDate(
            PointDeVenteDTO pointDeVente,
            Date dateDebut,
            Date dateFin) {

        Date dateFinInclusive =
                rendreDateFinInclusive(
                        dateFin
                );

        return venteRepository
                .findByPointDeVenteAndDateVenteBetween(
                        dtoMapper.fromPointDeVenteDTO(
                                pointDeVente
                        ),
                        dateDebut,
                        dateFinInclusive
                )
                .stream()
                .map(dtoMapper::fromVente)
                .toList();
    }


    // ============================================================
    // UTILITAIRE DATE
    // ============================================================

    private Date rendreDateFinInclusive(
            Date dateFin) {

        if (dateFin == null) {
            return null;
        }

        Calendar calendar =
                Calendar.getInstance();

        calendar.setTime(dateFin);

        calendar.set(
                Calendar.HOUR_OF_DAY,
                23
        );

        calendar.set(
                Calendar.MINUTE,
                59
        );

        calendar.set(
                Calendar.SECOND,
                59
        );

        calendar.set(
                Calendar.MILLISECOND,
                999
        );

        return calendar.getTime();
    }


    // ============================================================
    // RECUS
    // ============================================================

    @Override
    public RecuDTO saveRecu(
            RecuDTO recuDTO) {

        Recu recu =
                dtoMapper.fromRecuDTO(
                        recuDTO
                );

        Recu savedRecu =
                recuRepository.save(recu);

        return dtoMapper.fromRecu(
                savedRecu
        );
    }


    // ============================================================
    // MODIFIER RECU
    // ============================================================

    @Override
    public RecuDTO updateRecu(
            Long id,
            RecuDTO recuDTO)
            throws RecuNotFoundException {

        Recu recu =
                recuRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RecuNotFoundException(
                                        "Recu not found"
                                )
                        );

        dtoMapper.updateRecuFromDto(
                recuDTO,
                recu
        );

        Recu updatedRecu =
                recuRepository.save(recu);

        return dtoMapper.fromRecu(
                updatedRecu
        );
    }


    // ============================================================
    // DELETE RECU
    // ============================================================

    @Override
    public void deleteRecu(
            Long id)
            throws RecuNotFoundException {

        Recu recu =
                recuRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RecuNotFoundException(
                                        "Recu not found"
                                )
                        );

        recuRepository.delete(recu);
    }


    // ============================================================
    // GET RECU
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public RecuDTO getRecu(
            Long id)
            throws RecuNotFoundException {

        Recu recu =
                recuRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RecuNotFoundException(
                                        "Recu not found"
                                )
                        );

        return dtoMapper.fromRecu(recu);
    }


    // ============================================================
    // TOUS LES RECUS
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<RecuDTO> getAllRecus() {

        return recuRepository
                .findAll()
                .stream()
                .map(dtoMapper::fromRecu)
                .toList();
    }


    // ============================================================
    // RECUS PAR VENTE
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public RecuDTO getRecusByVente(
            VenteDTO venteDTO)
            throws RecuNotFoundException {

        Recu recu =
                recuRepository
                        .findByVente(
                                dtoMapper.fromVenteDTO(
                                        venteDTO
                                )
                        )
                        .orElseThrow(() ->
                                new RecuNotFoundException(
                                        "Recu not found"
                                )
                        );

        return dtoMapper.fromRecu(recu);
    }


    // ============================================================
    // RECUS PAR MODELE RECU
    // ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<RecuDTO> getRecusByModelRecu(
            ModeleRecuDTO modeleRecuDTO) {

        return recuRepository
                .findByModele(
                        dtoMapper.fromModeleRecuDTO(
                                modeleRecuDTO
                        )
                )
                .stream()
                .map(dtoMapper::fromRecu)
                .toList();
    }
    @Override
    @Transactional(readOnly = true)
    public List<VenteParArticleDTO> getVentesParArticle(
            String emailUtilisateur,
            Long pointDeVenteId,
            Date dateDebut,
            Date dateFin) {

        if (emailUtilisateur == null
                || emailUtilisateur.isBlank()) {

            throw new IllegalArgumentException(
                    "Utilisateur connecté introuvable"
            );
        }

        if (dateDebut == null || dateFin == null) {
            throw new IllegalArgumentException(
                    "Les dates de début et de fin sont obligatoires"
            );
        }

        if (dateDebut.after(dateFin)) {
            throw new IllegalArgumentException(
                    "La date de début doit être antérieure ou égale à la date de fin"
            );
        }

        Restaurant restaurant = getRestaurantPourUtilisateur(emailUtilisateur);

        Long restaurantId = restaurant.getId_restaurant();

        // Vérification du PDV
        if (pointDeVenteId != null) {

            boolean pdvAutorise =
                    restaurant.getPdvs() != null
                            && restaurant.getPdvs()
                            .stream()
                            .anyMatch(pdv ->
                                    pdv.getId_pdv() != null
                                            && pdv.getId_pdv()
                                            .equals(pointDeVenteId)
                            );

            if (!pdvAutorise) {
                throw new IllegalArgumentException(
                        "Le point de vente "
                                + pointDeVenteId
                                + " n'appartient pas à votre restaurant"
                );
            }
        }

        Date dateFinInclusive =
                rendreDateFinInclusive(dateFin);

        List<Object[]> resultats =
                venteRepository.getVentesParArticle(
                        restaurantId,
                        pointDeVenteId,
                        dateDebut,
                        dateFinInclusive
                );

        return resultats.stream()
                .map(row -> {

                    Long articleId =
                            ((Number) row[0]).longValue();

                    String article =
                            (String) row[1];

                    String categorie =
                            row[2] != null
                                    ? (String) row[2]
                                    : "Sans catégorie";

                    Integer quantite =
                            ((Number) row[3]).intValue();

                    Double chiffreAffaires =
                            ((Number) row[4]).doubleValue();

                    Double cout =
                            ((Number) row[5]).doubleValue();

                    Double marge =
                            chiffreAffaires - cout;

                    return new VenteParArticleDTO(
                            articleId,
                            article,
                            categorie,
                            quantite,
                            arrondir(chiffreAffaires),
                            arrondir(cout),
                            arrondir(marge)
                    );
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VenteParCategorieDTO> getVentesParCategorie(
            String emailUtilisateur,
            Long pointDeVenteId,
            Date dateDebut,
            Date dateFin) {

        // ========================================================
        // VALIDATION UTILISATEUR
        // ========================================================

        if (emailUtilisateur == null
                || emailUtilisateur.isBlank()) {

            throw new IllegalArgumentException(
                    "Utilisateur connecté introuvable"
            );
        }


        // ========================================================
        // VALIDATION DATES
        // ========================================================

        if (dateDebut == null || dateFin == null) {

            throw new IllegalArgumentException(
                    "Les dates de début et de fin sont obligatoires"
            );
        }


        if (dateDebut.after(dateFin)) {

            throw new IllegalArgumentException(
                    "La date de début doit être antérieure ou égale à la date de fin"
            );
        }


        // ========================================================
        // UTILISATEUR CONNECTÉ
        // ========================================================

        Restaurant restaurant = getRestaurantPourUtilisateur(emailUtilisateur);

        Long restaurantId = restaurant.getId_restaurant();


        // ========================================================
        // VÉRIFICATION DU PDV
        // ========================================================

        if (pointDeVenteId != null) {

            boolean pdvAutorise =
                    restaurant.getPdvs() != null
                            && restaurant.getPdvs()
                            .stream()
                            .anyMatch(pdv ->
                                    pdv.getId_pdv() != null
                                            && pdv.getId_pdv()
                                            .equals(pointDeVenteId)
                            );


            if (!pdvAutorise) {

                throw new IllegalArgumentException(
                        "Le point de vente "
                                + pointDeVenteId
                                + " n'appartient pas à votre restaurant"
                );
            }
        }


        // ========================================================
        // DATE FIN INCLUSIVE
        // ========================================================

        Date dateFinInclusive =
                rendreDateFinInclusive(dateFin);


        // ========================================================
        // RECHERCHE
        // ========================================================

        List<Object[]> resultats =
                venteRepository.getVentesParCategorie(
                        restaurantId,
                        pointDeVenteId,
                        dateDebut,
                        dateFinInclusive
                );


        // ========================================================
        // TRANSFORMATION DTO
        // ========================================================

        return resultats.stream()
                .map(row -> {

                    Long categorieId =
                            row[0] != null
                                    ? ((Number) row[0]).longValue()
                                    : null;


                    String categorie =
                            row[1] != null
                                    ? (String) row[1]
                                    : "Sans catégorie";


                    Integer quantiteVendue =
                            row[2] != null
                                    ? ((Number) row[2]).intValue()
                                    : 0;


                    Double chiffreAffaires =
                            row[3] != null
                                    ? ((Number) row[3]).doubleValue()
                                    : 0.0;


                    Double cout =
                            row[4] != null
                                    ? ((Number) row[4]).doubleValue()
                                    : 0.0;


                    Double margeBrute =
                            chiffreAffaires - cout;


                    return new VenteParCategorieDTO(
                            categorieId,
                            categorie,
                            quantiteVendue,
                            arrondir(chiffreAffaires),
                            arrondir(cout),
                            arrondir(margeBrute)
                    );

                })
                .toList();
    }



    // ============================================================
// VENTES PAR MODE DE PAIEMENT
// ============================================================

    @Override
    @Transactional(readOnly = true)
    public List<VenteParModePaiementDTO> getVentesParModePaiement(
            String emailUtilisateur,
            Long pointDeVenteId,
            Date dateDebut,
            Date dateFin) {

        // ========================================================
        // VALIDATION UTILISATEUR
        // ========================================================

        if (emailUtilisateur == null
                || emailUtilisateur.isBlank()) {

            throw new IllegalArgumentException(
                    "Utilisateur connecté introuvable"
            );
        }


        // ========================================================
        // VALIDATION DATES
        // ========================================================

        if (dateDebut == null || dateFin == null) {

            throw new IllegalArgumentException(
                    "Les dates de début et de fin sont obligatoires"
            );
        }


        if (dateDebut.after(dateFin)) {

            throw new IllegalArgumentException(
                    "La date de début doit être antérieure ou égale à la date de fin"
            );
        }


        // ========================================================
        // UTILISATEUR CONNECTÉ
        // ========================================================

        Restaurant restaurant = getRestaurantPourUtilisateur(emailUtilisateur);

        Long restaurantId = restaurant.getId_restaurant();


        // ========================================================
        // VÉRIFICATION DU PDV
        // ========================================================

        if (pointDeVenteId != null) {

            boolean pdvAutorise =
                    restaurant.getPdvs() != null
                            && restaurant.getPdvs()
                            .stream()
                            .anyMatch(pdv ->
                                    pdv.getId_pdv() != null
                                            && pdv.getId_pdv()
                                            .equals(pointDeVenteId)
                            );


            if (!pdvAutorise) {

                throw new IllegalArgumentException(
                        "Le point de vente "
                                + pointDeVenteId
                                + " n'appartient pas à votre restaurant"
                );
            }
        }


        // ========================================================
        // DATE FIN INCLUSIVE
        // ========================================================

        Date dateFinInclusive =
                rendreDateFinInclusive(dateFin);


        // ========================================================
        // RECHERCHE
        // ========================================================

        List<Object[]> resultats =
                venteRepository.getVentesParModePaiement(
                        restaurantId,
                        pointDeVenteId,
                        dateDebut,
                        dateFinInclusive
                );


        // ========================================================
        // TOTAL CHIFFRE D'AFFAIRES
        //
        // On calcule d'abord le total pour déterminer
        // le pourcentage de chaque mode de paiement.
        // ========================================================

        double totalChiffreAffaires =
                resultats.stream()
                        .mapToDouble(row ->
                                row[3] != null
                                        ? ((Number) row[3]).doubleValue()
                                        : 0.0
                        )
                        .sum();


        final double totalChiffreAffairesFinal = totalChiffreAffaires;


        // ========================================================
        // TRANSFORMATION DTO
        // ========================================================

        return resultats.stream()
                .map(row -> {

                    Long modePaiementId =
                            row[0] != null
                                    ? ((Number) row[0]).longValue()
                                    : null;


                    String modePaiement =
                            row[1] != null
                                    ? (String) row[1]
                                    : "Sans mode de paiement";


                    Integer nombreVentes =
                            row[2] != null
                                    ? ((Number) row[2]).intValue()
                                    : 0;


                    Double chiffreAffaires =
                            row[3] != null
                                    ? ((Number) row[3]).doubleValue()
                                    : 0.0;


                    chiffreAffaires =
                            arrondir(chiffreAffaires);


                    double pourcentage = 0.0;


                    if (totalChiffreAffairesFinal > 0) {

                        pourcentage =
                                chiffreAffaires / totalChiffreAffairesFinal * 100.0;
                    }


                    pourcentage =
                            arrondir(pourcentage);


                    return new VenteParModePaiementDTO(
                            modePaiementId,
                            modePaiement,
                            nombreVentes,
                            chiffreAffaires,
                            pourcentage
                    );

                })
                .toList();
    }


    // ================================================================
    // VENTES PAR EMPLOYE (SERVEUR AYANT PRIS LA COMMANDE)
    // ================================================================

    @Override
    @Transactional(readOnly = true)
    public List<VenteParEmployeeDTO> getVentesParEmployee(
            String emailUtilisateur,
            Long pointDeVenteId,
            Date dateDebut,
            Date dateFin) {

        // ========================================================
        // VALIDATION UTILISATEUR
        // ========================================================

        if (emailUtilisateur == null
                || emailUtilisateur.isBlank()) {

            throw new IllegalArgumentException(
                    "Utilisateur connecté introuvable"
            );
        }


        // ========================================================
        // VALIDATION DATES
        // ========================================================

        if (dateDebut == null || dateFin == null) {

            throw new IllegalArgumentException(
                    "Les dates de début et de fin sont obligatoires"
            );
        }


        if (dateDebut.after(dateFin)) {

            throw new IllegalArgumentException(
                    "La date de début doit être antérieure ou égale à la date de fin"
            );
        }


        // ========================================================
        // UTILISATEUR CONNECTÉ
        // ========================================================

        Restaurant restaurant = getRestaurantPourUtilisateur(emailUtilisateur);

        Long restaurantId = restaurant.getId_restaurant();


        // ========================================================
        // VÉRIFICATION DU PDV
        // ========================================================

        if (pointDeVenteId != null) {

            boolean pdvAutorise =
                    restaurant.getPdvs() != null
                            && restaurant.getPdvs()
                            .stream()
                            .anyMatch(pdv ->
                                    pdv.getId_pdv() != null
                                            && pdv.getId_pdv()
                                            .equals(pointDeVenteId)
                            );


            if (!pdvAutorise) {

                throw new IllegalArgumentException(
                        "Le point de vente "
                                + pointDeVenteId
                                + " n'appartient pas à votre restaurant"
                );
            }
        }


        // ========================================================
        // DATE FIN INCLUSIVE
        // ========================================================

        Date dateFinInclusive =
                rendreDateFinInclusive(dateFin);


        // ========================================================
        // RECHERCHE
        // ========================================================

        List<Object[]> resultats =
                venteRepository.getVentesParEmployee(
                        restaurantId,
                        pointDeVenteId,
                        dateDebut,
                        dateFinInclusive
                );


        // ========================================================
        // TOTAL CHIFFRE D'AFFAIRES
        //
        // On calcule d'abord le total pour déterminer
        // le pourcentage de productivité de chaque employé.
        // ========================================================

        double totalChiffreAffaires =
                resultats.stream()
                        .mapToDouble(row ->
                                row[4] != null
                                        ? ((Number) row[4]).doubleValue()
                                        : 0.0
                        )
                        .sum();


        final double totalChiffreAffairesFinal = totalChiffreAffaires;


        // ========================================================
        // TRANSFORMATION DTO
        // ========================================================

        return resultats.stream()
                .map(row -> {

                    Long employeeId =
                            row[0] != null
                                    ? ((Number) row[0]).longValue()
                                    : null;


                    String nomComplet =
                            row[1] != null
                                    ? (String) row[1]
                                    : "Employé inconnu";


                    String matricule =
                            row[2] != null
                                    ? (String) row[2]
                                    : "";


                    Integer nombreVentes =
                            row[3] != null
                                    ? ((Number) row[3]).intValue()
                                    : 0;


                    Double chiffreAffaires =
                            row[4] != null
                                    ? ((Number) row[4]).doubleValue()
                                    : 0.0;


                    chiffreAffaires =
                            arrondir(chiffreAffaires);


                    double pourcentage = 0.0;


                    if (totalChiffreAffairesFinal > 0) {

                        pourcentage =
                                chiffreAffaires / totalChiffreAffairesFinal * 100.0;
                    }


                    pourcentage =
                            arrondir(pourcentage);


                    return new VenteParEmployeeDTO(
                            employeeId,
                            nomComplet,
                            matricule,
                            nombreVentes,
                            chiffreAffaires,
                            pourcentage
                    );

                })
                .toList();
    }


    // ================================================================
    // VENTES PAR MODIFICATEUR
    // ================================================================

    @Override
    @Transactional(readOnly = true)
    public List<VenteParModificateurDTO> getVentesParModificateur(
            String emailUtilisateur,
            Long pointDeVenteId,
            Date dateDebut,
            Date dateFin) {

        // ========================================================
        // VALIDATION UTILISATEUR
        // ========================================================

        if (emailUtilisateur == null
                || emailUtilisateur.isBlank()) {

            throw new IllegalArgumentException(
                    "Utilisateur connecté introuvable"
            );
        }


        // ========================================================
        // VALIDATION DATES
        // ========================================================

        if (dateDebut == null || dateFin == null) {

            throw new IllegalArgumentException(
                    "Les dates de début et de fin sont obligatoires"
            );
        }


        if (dateDebut.after(dateFin)) {

            throw new IllegalArgumentException(
                    "La date de début doit être antérieure ou égale à la date de fin"
            );
        }


        // ========================================================
        // UTILISATEUR CONNECTÉ
        // ========================================================

        Restaurant restaurant = getRestaurantPourUtilisateur(emailUtilisateur);

        Long restaurantId = restaurant.getId_restaurant();


        // ========================================================
        // VÉRIFICATION DU PDV
        // ========================================================

        if (pointDeVenteId != null) {

            boolean pdvAutorise =
                    restaurant.getPdvs() != null
                            && restaurant.getPdvs()
                            .stream()
                            .anyMatch(pdv ->
                                    pdv.getId_pdv() != null
                                            && pdv.getId_pdv()
                                            .equals(pointDeVenteId)
                            );


            if (!pdvAutorise) {

                throw new IllegalArgumentException(
                        "Le point de vente "
                                + pointDeVenteId
                                + " n'appartient pas à votre restaurant"
                );
            }
        }


        // ========================================================
        // DATE FIN INCLUSIVE
        // ========================================================

        Date dateFinInclusive =
                rendreDateFinInclusive(dateFin);


        // ========================================================
        // RECHERCHE
        // ========================================================

        List<Object[]> resultats =
                venteRepository.getVentesParModificateur(
                        restaurantId,
                        pointDeVenteId,
                        dateDebut,
                        dateFinInclusive
                );


        // ========================================================
        // NOMS DES PRODUITS ASSOCIÉS (par modificateur)
        // ========================================================
        //
        // Un modificateur pouvant désormais être associé à plusieurs
        // produits, on ne peut plus le récupérer directement dans la
        // requête d'agrégation ci-dessus (cela fausserait les sommes
        // via un produit cartésien). On le résout donc séparément,
        // en une seule requête par lot.

        List<Long> modificateurIds =
                resultats.stream()
                        .map(row -> row[0] != null ? ((Number) row[0]).longValue() : null)
                        .filter(java.util.Objects::nonNull)
                        .toList();

        Map<Long, String> produitsParModificateur =
                modificateurRepository.findAllById(modificateurIds).stream()
                        .collect(
                                Collectors.toMap(
                                        Modificateur::getId_modificateur,
                                        m -> m.getProduits() == null
                                                ? ""
                                                : m.getProduits().stream()
                                                .map(Produit::getNom)
                                                .collect(Collectors.joining(", "))
                                )
                        );


        // ========================================================
        // TRANSFORMATION DTO
        // ========================================================

        return resultats.stream()
                .map(row -> {

                    Long modificateurId =
                            row[0] != null
                                    ? ((Number) row[0]).longValue()
                                    : null;


                    String modificateur =
                            row[1] != null
                                    ? (String) row[1]
                                    : "Modificateur inconnu";


                    String produit =
                            produitsParModificateur.getOrDefault(modificateurId, "");


                    Integer quantiteVendue =
                            row[2] != null
                                    ? ((Number) row[2]).intValue()
                                    : 0;


                    Double chiffreAffaires =
                            row[3] != null
                                    ? ((Number) row[3]).doubleValue()
                                    : 0.0;


                    return new VenteParModificateurDTO(
                            modificateurId,
                            modificateur,
                            produit,
                            quantiteVendue,
                            arrondir(chiffreAffaires)
                    );

                })
                .toList();
    }


    // ================================================================
    // VENTES PAR REDUCTION
    // ================================================================

    @Override
    @Transactional(readOnly = true)
    public List<VenteParReductionDTO> getVentesParReduction(
            String emailUtilisateur,
            Long pointDeVenteId,
            Date dateDebut,
            Date dateFin) {

        // ========================================================
        // VALIDATION UTILISATEUR
        // ========================================================

        if (emailUtilisateur == null
                || emailUtilisateur.isBlank()) {

            throw new IllegalArgumentException(
                    "Utilisateur connecté introuvable"
            );
        }


        // ========================================================
        // VALIDATION DATES
        // ========================================================

        if (dateDebut == null || dateFin == null) {

            throw new IllegalArgumentException(
                    "Les dates de début et de fin sont obligatoires"
            );
        }


        if (dateDebut.after(dateFin)) {

            throw new IllegalArgumentException(
                    "La date de début doit être antérieure ou égale à la date de fin"
            );
        }


        // ========================================================
        // UTILISATEUR CONNECTÉ
        // ========================================================

        Restaurant restaurant = getRestaurantPourUtilisateur(emailUtilisateur);

        Long restaurantId = restaurant.getId_restaurant();


        // ========================================================
        // VÉRIFICATION DU PDV
        // ========================================================

        if (pointDeVenteId != null) {

            boolean pdvAutorise =
                    restaurant.getPdvs() != null
                            && restaurant.getPdvs()
                            .stream()
                            .anyMatch(pdv ->
                                    pdv.getId_pdv() != null
                                            && pdv.getId_pdv()
                                            .equals(pointDeVenteId)
                            );


            if (!pdvAutorise) {

                throw new IllegalArgumentException(
                        "Le point de vente "
                                + pointDeVenteId
                                + " n'appartient pas à votre restaurant"
                );
            }
        }


        // ========================================================
        // DATE FIN INCLUSIVE
        // ========================================================

        Date dateFinInclusive =
                rendreDateFinInclusive(dateFin);


        // ========================================================
        // RECHERCHE
        // ========================================================

        List<Object[]> resultats =
                venteRepository.getVentesParReduction(
                        restaurantId,
                        pointDeVenteId,
                        dateDebut,
                        dateFinInclusive
                );


        // ========================================================
        // TRANSFORMATION DTO
        // ========================================================

        return resultats.stream()
                .map(row -> {

                    Long reductionId =
                            row[0] != null
                                    ? ((Number) row[0]).longValue()
                                    : null;


                    String nomReduction =
                            row[1] != null
                                    ? (String) row[1]
                                    : "Réduction inconnue";


                    String type =
                            row[2] != null
                                    ? row[2].toString()
                                    : "";


                    Double valeur =
                            row[3] != null
                                    ? ((Number) row[3]).doubleValue()
                                    : 0.0;


                    Integer nombreVentes =
                            row[4] != null
                                    ? ((Number) row[4]).intValue()
                                    : 0;


                    Double montantReduction =
                            row[5] != null
                                    ? ((Number) row[5]).doubleValue()
                                    : 0.0;


                    Double chiffreAffaires =
                            row[6] != null
                                    ? ((Number) row[6]).doubleValue()
                                    : 0.0;


                    return new VenteParReductionDTO(
                            reductionId,
                            nomReduction,
                            type,
                            valeur,
                            nombreVentes,
                            arrondir(montantReduction),
                            arrondir(chiffreAffaires)
                    );

                })
                .toList();
    }


    // ================================================================
    // VENTES PAR REÇU
    // ================================================================

    @Override
    public List<VenteParRecuDTO> getVentesParRecu(
            String emailUtilisateur,
            Long pointDeVenteId,
            Date dateDebut,
            Date dateFin) {

        // ========================================================
        // VALIDATION UTILISATEUR
        // ========================================================

        if (emailUtilisateur == null
                || emailUtilisateur.isBlank()) {

            throw new IllegalArgumentException(
                    "Utilisateur connecté introuvable"
            );
        }


        // ========================================================
        // VALIDATION DATES
        // ========================================================

        if (dateDebut == null || dateFin == null) {

            throw new IllegalArgumentException(
                    "Les dates de début et de fin sont obligatoires"
            );
        }


        if (dateDebut.after(dateFin)) {

            throw new IllegalArgumentException(
                    "La date de début doit être antérieure ou égale à la date de fin"
            );
        }


        // ========================================================
        // UTILISATEUR CONNECTÉ
        // ========================================================

        Restaurant restaurant = getRestaurantPourUtilisateur(emailUtilisateur);

        Long restaurantId = restaurant.getId_restaurant();


        // ========================================================
        // VÉRIFICATION DU PDV
        // ========================================================

        if (pointDeVenteId != null) {

            boolean pdvAutorise =
                    restaurant.getPdvs() != null
                            && restaurant.getPdvs()
                            .stream()
                            .anyMatch(pdv ->
                                    pdv.getId_pdv() != null
                                            && pdv.getId_pdv()
                                            .equals(pointDeVenteId)
                            );


            if (!pdvAutorise) {

                throw new IllegalArgumentException(
                        "Le point de vente "
                                + pointDeVenteId
                                + " n'appartient pas à votre restaurant"
                );
            }
        }


        // ========================================================
        // DATE FIN INCLUSIVE
        // ========================================================

        Date dateFinInclusive =
                rendreDateFinInclusive(dateFin);


        // ========================================================
        // RECHERCHE
        // ========================================================

        List<Object[]> resultats =
                venteRepository.getVentesParRecu(
                        restaurantId,
                        pointDeVenteId,
                        dateDebut,
                        dateFinInclusive
                );


        // ========================================================
        // TRANSFORMATION DTO
        // ========================================================

        return resultats.stream()
                .map(row -> {

                    Long idVente =
                            row[0] != null
                                    ? ((Number) row[0]).longValue()
                                    : null;

                    String numeroRecu =
                            row[1] != null
                                    ? (String) row[1]
                                    : null;

                    Date dateEmission =
                            row[2] != null
                                    ? (Date) row[2]
                                    : null;

                    Date dateVenteRow =
                            row[3] != null
                                    ? (Date) row[3]
                                    : null;

                    Double montantTtc =
                            row[4] != null
                                    ? ((Number) row[4]).doubleValue()
                                    : 0.0;

                    String modePaiement =
                            row[5] != null
                                    ? (String) row[5]
                                    : "Sans mode de paiement";

                    String pointDeVenteNom =
                            row[6] != null
                                    ? (String) row[6]
                                    : "Sans point de vente";

                    return new VenteParRecuDTO(
                            idVente,
                            numeroRecu,
                            dateEmission,
                            dateVenteRow,
                            arrondir(montantTtc),
                            modePaiement,
                            pointDeVenteNom
                    );

                })
                .toList();
    }

    // ============================================================
    // VERIFICATION DU PDV
    // ============================================================

    private void verifierPointDeVente(
            Restaurant restaurant,
            Long pointDeVenteId) {

        if (pointDeVenteId == null) {
            return;
        }

        boolean pdvAutorise =
                restaurant.getPdvs() != null
                        && restaurant.getPdvs()
                        .stream()
                        .anyMatch(pdv ->
                                pdv.getId_pdv() != null
                                        && pdv.getId_pdv().equals(pointDeVenteId)
                        );

        if (!pdvAutorise) {
            throw new IllegalArgumentException(
                    "Le point de vente " + pointDeVenteId
                            + " n'appartient pas au restaurant sélectionné"
            );
        }
    }


    // ============================================================
    // RESTAURANT DU CONTEXTE COURANT
    // ============================================================
    //
    // Un SUPERADMIN n'est pas un Employee. Le restaurant courant
    // vient donc du JWT émis lors de la sélection du restaurant.
    // Un employé classique continue, lui, à utiliser Employee.restaurant.

    private Restaurant getRestaurantPourUtilisateur(String emailUtilisateur) {
        if (emailUtilisateur == null || emailUtilisateur.isBlank()) {
            throw new IllegalArgumentException("Utilisateur connecté introuvable");
        }

        try {
            Long restaurantId = currentUserService.getRestaurantIdConnecte();

            if (restaurantId == null) {
                throw new IllegalArgumentException("Aucun restaurant sélectionné");
            }

            return restaurantRepository.findById(restaurantId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Restaurant introuvable : " + restaurantId));
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Impossible de déterminer le restaurant courant : " + e.getMessage(), e);
        }
    }

}