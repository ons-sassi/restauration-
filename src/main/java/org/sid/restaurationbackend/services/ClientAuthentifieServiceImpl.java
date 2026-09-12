package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.ClientAuthentifieDTO;
import org.sid.restaurationbackend.entities.ClientAuthentifie;
import org.sid.restaurationbackend.enums.StatutUtilisateur;
import org.sid.restaurationbackend.exceptions.ClientNotFoundException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.ClientAuthentifieRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class ClientAuthentifieServiceImpl implements ClientAuthentifieService {

    private final RestaurantMapper dtotMapper;
    private final ClientAuthentifieRepository clientAuthentifieRepository;



    @Override
    public ClientAuthentifieDTO saveClientAuthentifie(
            ClientAuthentifieDTO clientAuthentifieDTO) {

        ClientAuthentifie clientAuthentifie =
                dtotMapper.fromClientAuthentifieDTO(clientAuthentifieDTO);

        Date maintenant = new Date();


        clientAuthentifie.setDate_creation(maintenant);


        clientAuthentifie.setDate_modification_profil(maintenant);
ClientAuthentifie savedClient =
                clientAuthentifieRepository.save(clientAuthentifie);

        return dtotMapper.fromClientAuthentifie(savedClient);
    }



    @Override
    public ClientAuthentifieDTO updateClientAuthentifie(
            Long id,
            ClientAuthentifieDTO clientAuthentifieDTO)
            throws ClientNotFoundException {

        ClientAuthentifie clientAuthentifie =
                clientAuthentifieRepository.findById(id)
                        .orElseThrow(() ->
                                new ClientNotFoundException(
                                        "Client authentifié not found"));


        dtotMapper.updateClientAuthentifieFromDto(
                clientAuthentifieDTO,
                clientAuthentifie
        );

        clientAuthentifie.setDate_modification_profil(new Date());

        ClientAuthentifie updatedClient =
                clientAuthentifieRepository.save(clientAuthentifie);

        return dtotMapper.fromClientAuthentifie(updatedClient);
    }



    @Override
    public void deleteClientAuthentifie(Long id)
            throws ClientNotFoundException {

        ClientAuthentifie clientAuthentifie =
                clientAuthentifieRepository.findById(id)
                        .orElseThrow(() ->
                                new ClientNotFoundException(
                                        "Client authentifié not found"));

        clientAuthentifieRepository.delete(clientAuthentifie);
    }




    @Override
    public ClientAuthentifieDTO getClientAuthentifie(Long id)
            throws ClientNotFoundException {

        ClientAuthentifie clientAuthentifie =
                clientAuthentifieRepository.findById(id)
                        .orElseThrow(() ->
                                new ClientNotFoundException(
                                        "Client authentifié not found"));

        return dtotMapper.fromClientAuthentifie(clientAuthentifie);
    }




    @Override
    public List<ClientAuthentifieDTO> getAllClientAuthentifies() {

        return clientAuthentifieRepository.findAll()
                .stream()
                .map(dtotMapper::fromClientAuthentifie)
                .toList();
    }



    @Override
    public List<ClientAuthentifieDTO> searchClientAuthentifies(
            String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return getAllClientAuthentifies();
        }

        keyword = keyword.trim();

        return clientAuthentifieRepository
                .findByNomContainingIgnoreCaseOrPrenomContainingIgnoreCaseOrEmailContainingIgnoreCaseOrTelephoneContainingIgnoreCase(
                        keyword,
                        keyword,
                        keyword,
                        keyword
                )
                .stream()
                .map(dtotMapper::fromClientAuthentifie)
                .toList();
    }




    @Override
    public List<ClientAuthentifieDTO> getClientsByStatut(
            StatutUtilisateur statut) {

        return clientAuthentifieRepository
                .findByStatut(statut)
                .stream()
                .map(dtotMapper::fromClientAuthentifie)
                .toList();
    }


    @Override
    public void updateDerniereConnexion(Long id)
            throws ClientNotFoundException {

        ClientAuthentifie clientAuthentifie =
                clientAuthentifieRepository.findById(id)
                        .orElseThrow(() ->
                                new ClientNotFoundException(
                                        "Client authentifié not found"));

        clientAuthentifie.setDate_derniere_connection(new Date());

        clientAuthentifieRepository.save(clientAuthentifie);
    }
    @Override
    public ClientAuthentifieDTO desactiverClient(Long id)
            throws ClientNotFoundException {

        ClientAuthentifie clientAuthentifie =
                clientAuthentifieRepository.findById(id)
                        .orElseThrow(() ->
                                new ClientNotFoundException(
                                        "Client authentifié not found"));

        clientAuthentifie.setStatut(
                StatutUtilisateur.INACTIF
        );

        ClientAuthentifie savedClient =
                clientAuthentifieRepository.save(clientAuthentifie);

        return dtotMapper.fromClientAuthentifie(savedClient);
    }



    @Override
    public ClientAuthentifieDTO activerClient(Long id)
            throws ClientNotFoundException {

        ClientAuthentifie clientAuthentifie =
                clientAuthentifieRepository.findById(id)
                        .orElseThrow(() ->
                                new ClientNotFoundException(
                                        "Client authentifié not found"));

        clientAuthentifie.setStatut(
                StatutUtilisateur.ACTIF
        );

        ClientAuthentifie savedClient =
                clientAuthentifieRepository.save(clientAuthentifie);

        return dtotMapper.fromClientAuthentifie(savedClient);
    }

    @Override
    public ClientAuthentifieDTO getClientByCodeParrainage(String codeParrainage)
            throws ClientNotFoundException {

        ClientAuthentifie client = clientAuthentifieRepository
                .findByCodeParrainage(codeParrainage)
                .orElseThrow(() ->
                        new ClientNotFoundException(
                                "Aucun client trouvé avec ce code de parrainage"
                        ));

        return dtotMapper.fromClientAuthentifie(client);
    }

    @Override
    public ClientAuthentifieDTO updatePreferences(
            Long id,
            String preferences)
            throws ClientNotFoundException {

        ClientAuthentifie client = clientAuthentifieRepository.findById(id)
                .orElseThrow(() ->
                        new ClientNotFoundException(
                                "Client authentifié not found"
                        ));

        client.setPreferences(preferences);
        client.setDate_modification_profil(new Date());

        return dtotMapper.fromClientAuthentifie(
                clientAuthentifieRepository.save(client)
        );
    }
    @Override
    public ClientAuthentifieDTO updateAllergies(
            Long id,
            String allergies)
            throws ClientNotFoundException {

        ClientAuthentifie client = clientAuthentifieRepository.findById(id)
                .orElseThrow(() ->
                        new ClientNotFoundException(
                                "Client authentifié not found"
                        ));

        client.setAllergie(allergies);
        client.setDate_modification_profil(new Date());

        return dtotMapper.fromClientAuthentifie(
                clientAuthentifieRepository.save(client)
        );
    }

    @Override
    public ClientAuthentifieDTO getClientConnecte()
            throws ClientNotFoundException {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new ClientNotFoundException(
                    "Client non authentifié"
            );
        }

        String email = authentication.getName();

        ClientAuthentifie client =
                clientAuthentifieRepository
                        .findByEmail(email)
                        .orElseThrow(() ->
                                new ClientNotFoundException(
                                        "Client authentifié introuvable"
                                )
                        );

        return dtotMapper.fromClientAuthentifie(client);
    }

}