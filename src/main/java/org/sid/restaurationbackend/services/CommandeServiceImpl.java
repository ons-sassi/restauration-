package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.entities.*;
import org.sid.restaurationbackend.enums.StatutCommande;
import org.sid.restaurationbackend.enums.StatutReservation;
import org.sid.restaurationbackend.enums.StatutTable;
import org.sid.restaurationbackend.exceptions.*;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class CommandeServiceImpl implements CommandeService {

    private final RestaurantMapper dtotMapper;

    private final CommandeRepository commandeRepository;
    private final LigneCommandeRepository ligneCommandeRepository;

    private final ClientAuthentifieRepository clientAuthentifieRepository;
    private final ClientNonAuthentifieRepository clientNonAuthentifieRepository;
    private final TableRepository tableRestaurantRepository;
    private final EmployeeRepository employeeRepository;
    private final ReservationRepository reservationRepository;






    @Override
    public CommandeDTO updateCommande(
            Long id,
            CommandeDTO commandeDTO)
            throws CommandeNotFoundException {

        Commande commande =
                commandeRepository.findById(id)
                        .orElseThrow(() ->
                                new CommandeNotFoundException(
                                        "Commande not found"
                                ));

        dtotMapper.updateCommandeFromDto(
                commandeDTO,
                commande
        );

        Commande updatedCommande =
                commandeRepository.save(commande);

        return dtotMapper.fromCommande(updatedCommande);
    }


    @Override
    public void deleteCommande(Long id)
            throws CommandeNotFoundException {

        Commande commande =
                commandeRepository.findById(id)
                        .orElseThrow(() ->
                                new CommandeNotFoundException(
                                        "Commande not found"
                                ));

        commandeRepository.delete(commande);
    }


    @Override
    public CommandeDTO getCommande(Long id)
            throws CommandeNotFoundException {

        Commande commande =
                commandeRepository.findById(id)
                        .orElseThrow(() ->
                                new CommandeNotFoundException(
                                        "Commande not found"
                                ));

        return dtotMapper.fromCommande(commande);
    }


    @Override
    public List<CommandeDTO> getAllCommandes() {

        return commandeRepository.findAll()
                .stream()
                .map(dtotMapper::fromCommande)
                .toList();
    }




    @Override
    public List<CommandeDTO> searchCommandes(
            Long clientId,
            StatutCommande statut,
            Date dateDebut,
            Date dateFin)
            throws ClientNotFoundException {

        ClientAuthentifie client = null;

        if (clientId != null) {

            client = clientAuthentifieRepository
                    .findById(clientId)
                    .orElseThrow(() ->
                            new ClientNotFoundException(
                                    "Client not found"
                            ));
        }

        return commandeRepository.search(
                        client,
                        statut,
                        dateDebut,
                        dateFin
                )
                .stream()
                .map(dtotMapper::fromCommande)
                .toList();
    }


    @Override
    public List<CommandeDTO> getCommandesByClient(
            Long clientId)
            throws ClientNotFoundException {

        ClientAuthentifie client =
                clientAuthentifieRepository.findById(clientId)
                        .orElseThrow(() ->
                                new ClientNotFoundException(
                                        "Client not found"
                                ));

        return commandeRepository
                .findByClient(client)
                .stream()
                .map(dtotMapper::fromCommande)
                .toList();
    }


    @Override
    public List<CommandeDTO> getCommandesByClientNonAuthentifie(
            Long sessionId)
            throws ClientNotFoundException {

        ClientNonAuthentifie client =
                clientNonAuthentifieRepository
                        .findById(sessionId)
                        .orElseThrow(() ->
                                new ClientNotFoundException(
                                        "Session client not found"
                                ));

        return commandeRepository
                .findByClientNonAuthentifie(client)
                .stream()
                .map(dtotMapper::fromCommande)
                .toList();
    }


    @Override
    public List<CommandeDTO> getCommandesByTable(
            Long tableId) throws TableNotFoundException {

        TableRestaurant table =
                tableRestaurantRepository.findById(tableId)
                        .orElseThrow(() ->
                                new TableNotFoundException(
                                        "Table not found"
                                ));

        return commandeRepository
                .findByTable(table)
                .stream()
                .map(dtotMapper::fromCommande)
                .toList();
    }


    @Override
    public List<CommandeDTO> getCommandesByEmployee(
            Long employeeId)
            throws EmployeeNotFoundException {

        Employee employee =
                employeeRepository.findById(employeeId)
                        .orElseThrow(() ->
                                new EmployeeNotFoundException(
                                        "Employee not found"
                                ));

        return commandeRepository
                .findByEmployee(employee)
                .stream()
                .map(dtotMapper::fromCommande)
                .toList();
    }


    @Override
    public List<CommandeDTO> getCommandesByStatut(
            StatutCommande statut) {

        return commandeRepository
                .findByStatut(statut)
                .stream()
                .map(dtotMapper::fromCommande)
                .toList();
    }


    @Override
    public List<CommandeDTO> getCommandesByDate(
            Date dateDebut,
            Date dateFin) {

        return commandeRepository
                .findByDateCommandeBetween(
                        dateDebut,
                        dateFin
                )
                .stream()
                .map(dtotMapper::fromCommande)
                .toList();
    }




    @Override
    public CommandeDTO changerStatut(
            Long commandeId,
            StatutCommande statut)
            throws CommandeNotFoundException {

        Commande commande =
                commandeRepository.findById(commandeId)
                        .orElseThrow(() ->
                                new CommandeNotFoundException(
                                        "Commande not found"
                                ));

        commande.setStatut(statut);

        Commande savedCommande =
                commandeRepository.save(commande);

        return dtotMapper.fromCommande(savedCommande);
    }




    @Override
    public CommandeDTO annulerCommande(
            Long commandeId)
            throws CommandeNotFoundException {

        Commande commande =
                commandeRepository.findById(commandeId)
                        .orElseThrow(() ->
                                new CommandeNotFoundException(
                                        "Commande not found"
                                ));

        commande.setStatut(StatutCommande.ANNULEE);

        TableRestaurant table = commande.getTable();

        // Si la commande est rattachée à une table, on vérifie s'il
        // existe une réservation active (EN_ATTENTE ou CONFIRMEE) pour
        // cette table à la même date que la commande. Si c'est le cas,
        // cette réservation est également annulée et la table libérée.
        if (table != null && commande.getDateCommande() != null) {

            LocalDate dateCommande =
                    commande.getDateCommande()
                            .toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

            List<Reservation> reservationsActives =
                    reservationRepository
                            .findByTable(table)
                            .stream()
                            .filter(reservation ->
                                    reservation.getDateReservation() != null &&
                                            reservation.getDateReservation().equals(dateCommande) &&
                                            (reservation.getStatut() == StatutReservation.EN_ATTENTE ||
                                                    reservation.getStatut() == StatutReservation.CONFIRMEE)
                            )
                            .toList();

            if (!reservationsActives.isEmpty()) {

                for (Reservation reservation : reservationsActives) {
                    reservation.setStatut(StatutReservation.ANNULEE);
                    reservationRepository.save(reservation);
                }

                table.setStatut(StatutTable.LIBRE);
                tableRestaurantRepository.save(table);
            }
        }

        Commande savedCommande =
                commandeRepository.save(commande);

        return dtotMapper.fromCommande(savedCommande);
    }


    // Ajouté au Lot 2.5 (sécurité multi-restaurant) : permet au
    // contrôleur de recharger la vraie ligne (et sa commande, donc son
    // restaurant réel) avant modifierLigne/supprimerLigne, qui n'agissent
    // que sur un id de ligne sans passer par la commande.
    @Override
    public LigneCommandeDTO getLigneCommande(
            Long ligneId)
            throws LigneCommandeNotFoundException {

        LigneCommande ligne =
                ligneCommandeRepository.findById(ligneId)
                        .orElseThrow(() ->
                                new LigneCommandeNotFoundException(
                                        "Ligne commande not found"
                                ));

        return dtotMapper.fromLigneCommande(ligne);
    }


    @Override
    public LigneCommandeDTO ajouterLigne(
            Long commandeId,
            LigneCommandeDTO ligneDTO)
            throws CommandeNotFoundException {

        Commande commande =
                commandeRepository.findById(commandeId)
                        .orElseThrow(() ->
                                new CommandeNotFoundException(
                                        "Commande not found"
                                ));

        LigneCommande ligne =
                dtotMapper.fromLigneCommandeDTO(ligneDTO);

        ligne.setCommande(commande);

        LigneCommande savedLigne =
                ligneCommandeRepository.save(ligne);


        recalculerMontantTotal(commandeId);

        return dtotMapper.fromLigneCommande(savedLigne);
    }


    @Override
    public LigneCommandeDTO modifierLigne(
            Long ligneId,
            LigneCommandeDTO ligneDTO)
            throws LigneCommandeNotFoundException, CommandeNotFoundException {

        LigneCommande ligne =
                ligneCommandeRepository.findById(ligneId)
                        .orElseThrow(() ->
                                new LigneCommandeNotFoundException(
                                        "Ligne commande not found"
                                ));

        dtotMapper.updateLigneCommandeFromDto(
                ligneDTO,
                ligne
        );

        LigneCommande updatedLigne =
                ligneCommandeRepository.save(ligne);

        if (ligne.getCommande() != null) {
            recalculerMontantTotal(
                    ligne.getCommande().getId_commande()
            );
        }

        return dtotMapper.fromLigneCommande(updatedLigne);
    }


    @Override
    public void supprimerLigne(
            Long ligneId)
            throws LigneCommandeNotFoundException, CommandeNotFoundException {

        LigneCommande ligne =
                ligneCommandeRepository.findById(ligneId)
                        .orElseThrow(() ->
                                new LigneCommandeNotFoundException(
                                        "Ligne commande not found"
                                ));

        Long commandeId = null;

        if (ligne.getCommande() != null) {
            commandeId =
                    ligne.getCommande().getId_commande();
        }

        ligneCommandeRepository.delete(ligne);

        if (commandeId != null) {
            recalculerMontantTotal(commandeId);
        }
    }


    @Override
    public List<LigneCommandeDTO> getLignesCommande(
            Long commandeId)
            throws CommandeNotFoundException {

        Commande commande =
                commandeRepository.findById(commandeId)
                        .orElseThrow(() ->
                                new CommandeNotFoundException(
                                        "Commande not found"
                                ));

        if (commande.getLignes() == null) {
            return List.of();
        }

        return commande.getLignes()
                .stream()
                .map(dtotMapper::fromLigneCommande)
                .toList();
    }



    @Override
    public Double calculerMontantTotal(
            Long commandeId)
            throws CommandeNotFoundException {

        Commande commande =
                commandeRepository.findById(commandeId)
                        .orElseThrow(() ->
                                new CommandeNotFoundException(
                                        "Commande not found"
                                ));

        if (commande.getLignes() == null) {
            return 0.0;
        }

        return commande.getLignes()
                .stream()
                .mapToDouble(ligne ->
                        ligne.getQuantite()
                                * ligne.getPrix_unitaire())
                .sum();
    }


    @Override
    public CommandeDTO recalculerMontantTotal(
            Long commandeId)
            throws CommandeNotFoundException {

        Commande commande =
                commandeRepository.findById(commandeId)
                        .orElseThrow(() ->
                                new CommandeNotFoundException(
                                        "Commande not found"
                                ));

        double total = 0.0;

        if (commande.getLignes() != null) {

            total = commande.getLignes()
                    .stream()
                    .mapToDouble(ligne ->
                            ligne.getQuantite()
                                    * ligne.getPrix_unitaire())
                    .sum();
        }

        commande.setMontant_total(total);

        Commande savedCommande =
                commandeRepository.save(commande);

        return dtotMapper.fromCommande(savedCommande);
    }


    @Override
    public CommandeDTO creerCommandeClient(
            CommandeDTO commandeDTO) {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getName() == null ||
                authentication.getName().isBlank()) {

            throw new IllegalStateException(
                    "Client non authentifié"
            );
        }

        String email = authentication.getName();

        ClientAuthentifie client =
                clientAuthentifieRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Client authentifié introuvable"
                                )
                        );

        Commande commande =
                dtotMapper.fromCommandeDTO(commandeDTO);

        /*
         * IMPORTANT :
         * Le client vient du JWT.
         * On ignore donc le client éventuellement
         * envoyé par Angular.
         */
        commande.setClient(client);

        /*
         * Une commande authentifiée ne doit pas
         * être liée à une session anonyme.
         */
        commande.setClientNonAuthentifie(null);

        /*
         * La date est créée par le backend.
         */
        if (commande.getDateCommande() == null) {
            commande.setDateCommande(new Date());
        }

        /*
         * Le montant est recalculé côté backend.
         * Angular peut également le calculer pour
         * l'affichage, mais on ne lui fait pas confiance.
         */
        double total = 0.0;

        if (commande.getLignes() != null) {

            total = commande.getLignes()
                    .stream()
                    .mapToDouble(ligne -> {

                        double quantite =
                                ligne.getQuantite() != null
                                        ? ligne.getQuantite()
                                        : 0.0;

                        double prix =
                                ligne.getPrix_unitaire() != null
                                        ? ligne.getPrix_unitaire()
                                        : 0.0;

                        return quantite * prix;
                    })
                    .sum();
        }

        commande.setMontant_total(total);

        Commande savedCommande =
                commandeRepository.save(commande);

        return dtotMapper.fromCommande(savedCommande);
    }



    @Override
    public CommandeDTO creerCommandeAnonyme(
            CommandeDTO commandeDTO) {



        if (commandeDTO.getClientNonAuthentifie() == null) {

            throw new IllegalArgumentException(
                    "Une session client est obligatoire "
                            + "pour une commande anonyme"
            );
        }



        ClientNonAuthentifie session =
                clientNonAuthentifieRepository
                        .findById(
                                commandeDTO
                                        .getClientNonAuthentifie()
                                        .getId_session()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Session client introuvable"
                                )
                        );




        Commande commande =
                dtotMapper.fromCommandeDTO(commandeDTO);



        commande.setClientNonAuthentifie(session);




        commande.setClient(null);


        if (commande.getDateCommande() == null) {
            commande.setDateCommande(new Date());
        }


        if (commande.getLignes() != null) {

            double total =
                    commande.getLignes()
                            .stream()
                            .mapToDouble(ligne ->
                                    ligne.getQuantite()
                                            * ligne.getPrix_unitaire())
                            .sum();

            commande.setMontant_total(total);

        } else {

            commande.setMontant_total(0.0);
        }


        Commande savedCommande =
                commandeRepository.save(commande);


        return dtotMapper.fromCommande(savedCommande);
    }

    @Override
    public CommandeDTO getCommandeClient(
            Long commandeId,
            Long clientId)
            throws CommandeNotFoundException {

        Commande commande =
                commandeRepository.findById(commandeId)
                        .orElseThrow(() ->
                                new CommandeNotFoundException(
                                        "Commande not found"
                                )
                        );

        if (commande.getClient() == null ||
                commande.getClient().getId_utilisateur() == null ||
                !commande.getClient()
                        .getId_utilisateur()
                        .equals(clientId)) {

            throw new CommandeNotFoundException(
                    "Commande not found"
            );
        }

        return dtotMapper.fromCommande(commande);
    }



}