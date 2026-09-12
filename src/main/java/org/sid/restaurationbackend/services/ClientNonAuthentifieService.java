package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.ClientNonAuthentifieDTO;

import org.sid.restaurationbackend.dtos.RestaurantDTO;
import org.sid.restaurationbackend.dtos.TableRestaurantDTO;
import org.sid.restaurationbackend.enums.OrigineSession;
import org.sid.restaurationbackend.exceptions.ClientNotFoundException;

import java.util.List;

public interface ClientNonAuthentifieService {
    ClientNonAuthentifieDTO saveClientNonAuthentifie(ClientNonAuthentifieDTO clientNonAuthentifieDTO);
    ClientNonAuthentifieDTO getClientNonAuthentifieById(Long id_client_non_authentifie) throws ClientNotFoundException;
    ClientNonAuthentifieDTO updateClientNonAuthentifie(Long id_client_non_authentifie, ClientNonAuthentifieDTO clientNonAuthentifieDTO) throws ClientNotFoundException;
    void deleteClientNonAuthentifie(Long id_client_non_authentifie) throws ClientNotFoundException;
    List<ClientNonAuthentifieDTO> getAllClientNonAuthentifie();
    List<ClientNonAuthentifieDTO> getClientNonAuthentifiesByRestaurant(RestaurantDTO restaurant);
    List<ClientNonAuthentifieDTO> getClientNonAuthentifiesByTableRestaurant(TableRestaurantDTO tableRestaurantDTO);


    List<ClientNonAuthentifieDTO> getClientNonAuthentifiesByOrigineSession(
            OrigineSession origineSession);

    List<ClientNonAuthentifieDTO> getSessionsByRestaurantAndTable(
            RestaurantDTO restaurant,
            TableRestaurantDTO table);

    void fermerSession(Long id) throws ClientNotFoundException;

    boolean sessionActive(Long id)
            throws ClientNotFoundException;
}
