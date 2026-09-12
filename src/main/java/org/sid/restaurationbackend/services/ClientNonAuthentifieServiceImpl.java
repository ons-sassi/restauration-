package org.sid.restaurationbackend.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.entities.*;
import org.sid.restaurationbackend.enums.OrigineSession;
import org.sid.restaurationbackend.exceptions.ClientNotFoundException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
@Service
@Transactional
@AllArgsConstructor
@Slf4j

public class ClientNonAuthentifieServiceImpl implements ClientNonAuthentifieService {
    private RestaurantMapper dtotMapper;
    private ClientNonAuthentifieRepository clientNonAuthentifieRepository;
    @Override
    public ClientNonAuthentifieDTO saveClientNonAuthentifie(ClientNonAuthentifieDTO clientNonAuthentifieDTO) {
        ClientNonAuthentifie clientNonAuthentifie = dtotMapper.fromClientNonAuthentifieDTO(clientNonAuthentifieDTO);
        clientNonAuthentifie.setDate_debut_session(new Date());
        ClientNonAuthentifie savedClientNonAuthentifie = clientNonAuthentifieRepository.save(clientNonAuthentifie);
        return dtotMapper.fromClientNonAuthentifie(savedClientNonAuthentifie);
    }

    @Override
    public ClientNonAuthentifieDTO getClientNonAuthentifieById(Long id_client_non_authentifie) throws ClientNotFoundException {
        ClientNonAuthentifie clientNonAuthentifie = clientNonAuthentifieRepository.findById(id_client_non_authentifie)
                .orElseThrow(() -> new ClientNotFoundException("Client non authentifié not found"));
        return dtotMapper.fromClientNonAuthentifie(clientNonAuthentifie);

    }

    @Override
    public ClientNonAuthentifieDTO updateClientNonAuthentifie(Long id_client_non_authentifie, ClientNonAuthentifieDTO clientNonAuthentifieDTO) throws ClientNotFoundException {

        ClientNonAuthentifie clientNonAuthentifie = clientNonAuthentifieRepository.findById(id_client_non_authentifie)
                .orElseThrow(() -> new ClientNotFoundException("Client non authentifié not found"));

        dtotMapper.updateClientNonAuthentifieFromDto(clientNonAuthentifieDTO, clientNonAuthentifie);

        ClientNonAuthentifie updatedClientNonAuthentifie = clientNonAuthentifieRepository.save(clientNonAuthentifie);
        return dtotMapper.fromClientNonAuthentifie(updatedClientNonAuthentifie);
    }

    @Override
    public void deleteClientNonAuthentifie(Long id_client_non_authentifie) throws ClientNotFoundException {
        ClientNonAuthentifie clientNonAuthentifie = clientNonAuthentifieRepository.findById(id_client_non_authentifie)
                .orElseThrow(() -> new ClientNotFoundException("Client non authentifié not found"));
        clientNonAuthentifieRepository.delete(clientNonAuthentifie);
    }

    @Override
    public List<ClientNonAuthentifieDTO> getAllClientNonAuthentifie() {

        return clientNonAuthentifieRepository.findAll().stream()
                .map(dtotMapper::fromClientNonAuthentifie)
                .toList();
    }



    @Override
    public List<ClientNonAuthentifieDTO> getClientNonAuthentifiesByRestaurant(RestaurantDTO restaurant) {
        return clientNonAuthentifieRepository.findByRestaurant(dtotMapper.fromRestaurantDTO(restaurant)).stream()
                .map(dtotMapper::fromClientNonAuthentifie)
                .toList();
    }

    @Override
    public List<ClientNonAuthentifieDTO> getClientNonAuthentifiesByTableRestaurant(   TableRestaurantDTO table_restaurant) {
        return clientNonAuthentifieRepository.findByTableScannee(dtotMapper.fromTableRestaurantDTO(table_restaurant)).stream()
                .map(dtotMapper::fromClientNonAuthentifie)
                .toList();
    }

    @Override
    public List<ClientNonAuthentifieDTO> getClientNonAuthentifiesByOrigineSession(
            OrigineSession origineSession) {

        return clientNonAuthentifieRepository
                .findByOrigineSession(origineSession)
                .stream()
                .map(dtotMapper::fromClientNonAuthentifie)
                .toList();
    }
    @Override
    public List<ClientNonAuthentifieDTO> getSessionsByRestaurantAndTable(
            RestaurantDTO restaurant,
            TableRestaurantDTO table) {

        return clientNonAuthentifieRepository
                .findByRestaurantAndTableScannee(
                        dtotMapper.fromRestaurantDTO(restaurant),
                        dtotMapper.fromTableRestaurantDTO(table)
                )
                .stream()
                .map(dtotMapper::fromClientNonAuthentifie)
                .toList();
    }

    @Override
    public void fermerSession(Long id) throws ClientNotFoundException {

        ClientNonAuthentifie client =
                clientNonAuthentifieRepository.findById(id)
                        .orElseThrow(() ->
                                new ClientNotFoundException(
                                        "Session client non authentifié not found"
                                ));

        client.setDate_fin_session(new Date());

        clientNonAuthentifieRepository.save(client);



    }

    @Override
    public boolean sessionActive(Long id)
            throws ClientNotFoundException {

        ClientNonAuthentifie client =
                clientNonAuthentifieRepository.findById(id)
                        .orElseThrow(() ->
                                new ClientNotFoundException(
                                        "Session client non authentifié not found"
                                ));

        return client.getDate_fin_session() == null;
    }
}
