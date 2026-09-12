package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.sid.restaurationbackend.dtos.DashboardCommandeDTO;
import org.sid.restaurationbackend.dtos.DashboardDTO;
import org.sid.restaurationbackend.dtos.VenteGraphDTO;

import org.sid.restaurationbackend.entities.ClientAuthentifie;
import org.sid.restaurationbackend.entities.Commande;
import org.sid.restaurationbackend.entities.PointDeVente;
import org.sid.restaurationbackend.entities.Restaurant;
import org.sid.restaurationbackend.entities.TableRestaurant;
import org.sid.restaurationbackend.entities.Vente;

import org.sid.restaurationbackend.enums.StatutCommande;
import org.sid.restaurationbackend.enums.StatutTable;

import org.sid.restaurationbackend.repositories.ClientAuthentifieRepository;
import org.sid.restaurationbackend.repositories.CommandeRepository;
import org.sid.restaurationbackend.repositories.PointDeVenteRepository;
import org.sid.restaurationbackend.repositories.RestaurantRepository;
import org.sid.restaurationbackend.repositories.TableRepository;
import org.sid.restaurationbackend.repositories.VenteRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final RestaurantRepository restaurantRepository;

    private final PointDeVenteRepository pointDeVenteRepository;

    private final TableRepository tableRepository;

    private final CommandeRepository commandeRepository;

    private final VenteRepository venteRepository;

    private final ClientAuthentifieRepository clientAuthentifieRepository;


    @Override
    public DashboardDTO getDashboard(
            Long restaurantId,
            LocalDate date
    ) {

        /*
         * ==========================================
         * RESTAURANT
         * ==========================================
         */

        Restaurant restaurant =
                restaurantRepository.findById(restaurantId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Restaurant introuvable : "
                                                + restaurantId
                                )
                        );


        /*
         * Si aucune date n'est envoyée,
         * on utilise aujourd'hui.
         */

        if (date == null) {
            date = LocalDate.now();
        }


        /*
         * ==========================================
         * DATE DU JOUR
         * ==========================================
         */

        Date debutJour =
                Date.from(
                        date.atStartOfDay(
                                ZoneId.systemDefault()
                        ).toInstant()
                );

        Date finJour =
                Date.from(
                        date.plusDays(1)
                                .atStartOfDay(
                                        ZoneId.systemDefault()
                                )
                                .toInstant()
                );


        /*
         * ==========================================
         * DATE D'HIER
         * ==========================================
         */

        LocalDate hier =
                date.minusDays(1);

        Date debutHier =
                Date.from(
                        hier.atStartOfDay(
                                ZoneId.systemDefault()
                        ).toInstant()
                );

        Date finHier =
                Date.from(
                        date.atStartOfDay(
                                ZoneId.systemDefault()
                        ).toInstant()
                );


        /*
         * ==========================================
         * PDV DU RESTAURANT
         * ==========================================
         */

        List<PointDeVente> pdvs =
                pointDeVenteRepository
                        .findByRestaurant(restaurant);


        /*
         * ==========================================
         * TABLES DU RESTAURANT
         * ==========================================
         */

        List<TableRestaurant> tables =
                tableRepository
                        .findByRestaurant(restaurant);


        long tablesOccupees =
                tables.stream()
                        .filter(table ->
                                table.getStatut()
                                        == StatutTable.OCCUPEE
                        )
                        .count();


        long nombreTables =
                tables.size();


        /*
         * ==========================================
         * VENTES DU JOUR
         * ==========================================
         */

        List<Vente> ventesJour =
                new ArrayList<>();


        for (PointDeVente pdv : pdvs) {

            venteRepository
                    .findByPointDeVenteAndDateVenteBetween(
                            pdv,
                            debutJour,
                            finJour
                    )
                    .forEach(ventesJour::add);
        }


        /*
         * ==========================================
         * TOTAL VENTES DU JOUR
         * ==========================================
         */

        double ventesDuJour =
                ventesJour.stream()
                        .mapToDouble(vente ->
                                vente.getMontantTtc() != null
                                        ? vente.getMontantTtc()
                                        : 0.0
                        )
                        .sum();


        /*
         * ==========================================
         * VENTES D'HIER
         * ==========================================
         */

        List<Vente> ventesHier =
                new ArrayList<>();


        for (PointDeVente pdv : pdvs) {

            venteRepository
                    .findByPointDeVenteAndDateVenteBetween(
                            pdv,
                            debutHier,
                            finHier
                    )
                    .forEach(ventesHier::add);
        }


        double montantVentesHier =
                ventesHier.stream()
                        .mapToDouble(vente ->
                                vente.getMontantTtc() != null
                                        ? vente.getMontantTtc()
                                        : 0.0
                        )
                        .sum();


        /*
         * ==========================================
         * COMMANDES DU JOUR
         * ==========================================
         */

        List<Commande> commandesJour =
                commandeRepository
                        .findByDateCommandeBetween(
                                debutJour,
                                finJour
                        )
                        .stream()
                        .filter(commande ->
                                appartientAuRestaurant(
                                        commande,
                                        restaurant
                                )
                        )
                        .toList();


        /*
         * ==========================================
         * COMMANDES D'HIER
         * ==========================================
         */

        List<Commande> commandesHier =
                commandeRepository
                        .findByDateCommandeBetween(
                                debutHier,
                                finHier
                        )
                        .stream()
                        .filter(commande ->
                                appartientAuRestaurant(
                                        commande,
                                        restaurant
                                )
                        )
                        .toList();


        /*
         * ==========================================
         * COMMANDES EN COURS
         *
         * EN_ATTENTE + SERVIE
         * ==========================================
         */

        long commandesEnCours =
                commandesJour.stream()
                        .filter(commande ->
                                commande.getStatut()
                                        == StatutCommande.EN_ATTENTE
                                        ||
                                        commande.getStatut()
                                                == StatutCommande.SERVIE
                        )
                        .count();


        /*
         * ==========================================
         * CLIENTS
         *
         * On compte les clients authentifiés
         * ayant passé une commande dans
         * ce restaurant.
         * ==========================================
         */

        Set<Long> clientIds =
                new HashSet<>();


        for (Commande commande : commandesJour) {

            if (commande.getClient() != null) {

                clientIds.add(
                        commande.getClient()
                                .getId_utilisateur()
                );
            }
        }


        long nombreClients =
                clientIds.size();


        /*
         * ==========================================
         * CLIENTS D'HIER
         * ==========================================
         */

        Set<Long> clientIdsHier =
                new HashSet<>();


        for (Commande commande : commandesHier) {

            if (commande.getClient() != null) {

                clientIdsHier.add(
                        commande.getClient()
                                .getId_utilisateur()
                );
            }
        }


        long clientsHier =
                clientIdsHier.size();


        /*
         * ==========================================
         * GRAPHIQUE
         * ==========================================
         */

        List<VenteGraphDTO> graphique =
                construireGraphique(
                        ventesJour
                );


        /*
         * ==========================================
         * DERNIÈRES COMMANDES
         * ==========================================
         */

        List<DashboardCommandeDTO>
                dernieresCommandes =
                commandesJour.stream()

                        .sorted(
                                Comparator.comparing(
                                        Commande::getDateCommande,
                                        Comparator.nullsLast(
                                                Comparator.reverseOrder()
                                        )
                                )
                        )

                        .limit(5)

                        .map(
                                this::convertirCommande
                        )

                        .toList();


        /*
         * ==========================================
         * RESULTAT
         * ==========================================
         */

        return new DashboardDTO(

                ventesDuJour,

                commandesEnCours,

                tablesOccupees,

                nombreTables,

                nombreClients,

                montantVentesHier,

                (long) commandesHier.size(),

                clientsHier,

                graphique,

                dernieresCommandes
        );
    }


    /*
     * ==================================================
     * VERIFIER SI UNE COMMANDE APPARTIENT AU RESTAURANT
     * ==================================================
     */

    private boolean appartientAuRestaurant(
            Commande commande,
            Restaurant restaurant
    ) {

        /*
         * Cas 1 :
         * la commande possède une table.
         */

        if (commande.getTable() != null) {

            if (commande.getTable()
                    .getRestaurant() != null) {

                return commande.getTable()
                        .getRestaurant()
                        .getId_restaurant()
                        .equals(
                                restaurant.getId_restaurant()
                        );
            }
        }


        /*
         * Cas 2 :
         * la commande possède un employé.
         *
         * L'employé possède un PDV,
         * et le PDV appartient au restaurant.
         */

        if (commande.getEmployee() != null &&
                commande.getEmployee()
                        .getPdvAffecte() != null &&
                commande.getEmployee()
                        .getPdvAffecte()
                        .getRestaurant() != null) {

            return commande.getEmployee()
                    .getPdvAffecte()
                    .getRestaurant()
                    .getId_restaurant()
                    .equals(
                            restaurant.getId_restaurant()
                    );
        }


        /*
         * Si aucun lien vers le restaurant
         * n'est disponible.
         */

        return false;
    }


    /*
     * ==================================================
     * GRAPHIQUE DES VENTES PAR HEURE
     * ==================================================
     */

    private List<VenteGraphDTO> construireGraphique(
            List<Vente> ventes
    ) {

        List<VenteGraphDTO> resultat =
                new ArrayList<>();


        /*
         * On crée les 24 heures.
         *
         * Même lorsqu'il n'y a aucune vente,
         * l'heure existe avec 0 €.
         */

        double[] montants =
                new double[24];


        for (Vente vente : ventes) {

            if (vente.getDateVente() == null) {
                continue;
            }


            Calendar calendar =
                    Calendar.getInstance();

            calendar.setTime(
                    vente.getDateVente()
            );


            int heure =
                    calendar.get(
                            Calendar.HOUR_OF_DAY
                    );


            double montant =
                    vente.getMontantTtc() != null
                            ? vente.getMontantTtc()
                            : 0.0;


            montants[heure] += montant;
        }


        for (int heure = 0; heure < 24; heure++) {

            resultat.add(
                    new VenteGraphDTO(
                            String.format(
                                    "%02dh",
                                    heure
                            ),
                            montants[heure]
                    )
            );
        }


        return resultat;
    }


    /*
     * ==================================================
     * CONVERSION COMMANDE → DASHBOARD DTO
     * ==================================================
     */

    private DashboardCommandeDTO convertirCommande(
            Commande commande
    ) {

        String client =
                "Client non authentifié";


        /*
         * Client authentifié
         */

        ClientAuthentifie
                clientAuthentifie =
                commande.getClient();


        if (clientAuthentifie != null) {

            String prenom =
                    clientAuthentifie.getPrenom();

            String nom =
                    clientAuthentifie.getNom();


            if (prenom != null &&
                    nom != null) {

                client =
                        prenom + " " + nom;

            } else if (nom != null) {

                client = nom;

            } else if (prenom != null) {

                client = prenom;
            }
        }


        /*
         * Table
         */

        String table =
                "À emporter";


        if (commande.getTable() != null) {

            Integer numero =
                    commande.getTable()
                            .getNumeroTable();

            if (numero != null) {

                table =
                        "Table " + numero;
            }
        }


        /*
         * Statut
         */

        String statut =
                commande.getStatut() != null
                        ? commande.getStatut().name()
                        : null;


        /*
         * Montant
         */

        Double montant =
                commande.getMontant_total();


        return new DashboardCommandeDTO(

                commande.getId_commande(),

                client,

                table,

                montant,

                statut
        );
    }
}