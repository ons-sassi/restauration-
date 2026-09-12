package org.sid.restaurationbackend.services;


import org.sid.restaurationbackend.dtos.*;
import org.sid.restaurationbackend.exceptions.PointDeVenteNotFoundException;
import org.sid.restaurationbackend.exceptions.RestaurantNotFoundException;

import java.util.List;

public interface PointDeVenteService {
    PointDeVenteDTO savePointDeVente(PointDeVenteDTO pointDeVenteDTO);
    PointDeVenteDTO updatePointDeVente(PointDeVenteDTO pointDeVenteDTO) throws PointDeVenteNotFoundException;
    void deletePointDeVente(Long id) throws PointDeVenteNotFoundException;
    PointDeVenteDTO getPointDeVente(Long id) throws PointDeVenteNotFoundException;
    List<PointDeVenteDTO> getAllPointsDeVente();
    List<PointDeVenteDTO> getPointDeVenteByNom_pdv(String name);


    List<PointDeVenteDTO> getPointDeVenteByRestaurant(
            Long restaurantId)
            throws RestaurantNotFoundException;

    List<PointDeVenteDTO> getPointDeVenteByStatutConnexion(
            String statutConnexion);
}
