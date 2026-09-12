package org.sid.restaurationbackend.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.PointDeVenteDTO;
import org.sid.restaurationbackend.dtos.RestaurantDTO;
import org.sid.restaurationbackend.entities.PointDeVente;
import org.sid.restaurationbackend.exceptions.PointDeVenteNotFoundException;
import org.sid.restaurationbackend.exceptions.RestaurantNotFoundException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.PointDeVenteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class PointDeVenteServiceImpl implements PointDeVenteService {

    private final RestaurantMapper dtotMapper;
    private final PointDeVenteRepository pointDeVenteRepository;
    private final RestaurantService restaurantService;

    @Override
    public PointDeVenteDTO savePointDeVente(
            PointDeVenteDTO pointDeVenteDTO) {

        PointDeVente pointDeVente =
                dtotMapper.fromPointDeVenteDTO(pointDeVenteDTO);

        PointDeVente savedPointDeVente =
                pointDeVenteRepository.save(pointDeVente);

        return dtotMapper.fromPointDeVente(savedPointDeVente);
    }

    @Override
    public PointDeVenteDTO updatePointDeVente(

            PointDeVenteDTO pointDeVenteDTO)
            throws PointDeVenteNotFoundException {

        PointDeVente pointDeVente =
                pointDeVenteRepository
                        .findById(pointDeVenteDTO.getId_pdv())
                        .orElseThrow(() ->
                                new PointDeVenteNotFoundException(
                                        "Point de vente not found"
                                ));

        dtotMapper.updatePointDeVenteFromDto(pointDeVenteDTO,pointDeVente);
        PointDeVente updatedPointDeVente = pointDeVenteRepository.save(pointDeVente);

        return dtotMapper.fromPointDeVente(updatedPointDeVente);


    }

    @Override
    public void deletePointDeVente(Long id)
            throws PointDeVenteNotFoundException {

        PointDeVente pointDeVente =
                pointDeVenteRepository.findById(id)
                        .orElseThrow(() ->
                                new PointDeVenteNotFoundException(
                                        "Point de vente not found"
                                ));

        pointDeVenteRepository.delete(pointDeVente);
    }

    @Override
    public PointDeVenteDTO getPointDeVente(Long id)
            throws PointDeVenteNotFoundException {

        PointDeVente pointDeVente =
                pointDeVenteRepository.findById(id)
                        .orElseThrow(() ->
                                new PointDeVenteNotFoundException(
                                        "Point de vente not found"
                                ));

        return dtotMapper.fromPointDeVente(pointDeVente);
    }

    @Override
    public List<PointDeVenteDTO> getAllPointsDeVente() {

        return pointDeVenteRepository.findAll()
                .stream()
                .map(dtotMapper::fromPointDeVente)
                .toList();
    }

    @Override
    public List<PointDeVenteDTO> getPointDeVenteByNom_pdv(
            String name) {

        if (name == null || name.isBlank()) {
            return getAllPointsDeVente();
        }

        return pointDeVenteRepository
                .findByNomPdvContainingIgnoreCase(name.trim())
                .stream()
                .map(dtotMapper::fromPointDeVente)
                .toList();
    }

    @Override
    public List<PointDeVenteDTO> getPointDeVenteByRestaurant(
            Long restaurantId)
            throws RestaurantNotFoundException {
            RestaurantDTO restaurantDTO =restaurantService.getRestaurant(restaurantId);

        return pointDeVenteRepository
                .findByRestaurant(dtotMapper.fromRestaurantDTO(restaurantDTO))
                .stream()
                .map(dtotMapper::fromPointDeVente)
                .toList();
    }

    @Override
    public List<PointDeVenteDTO> getPointDeVenteByStatutConnexion(
            String statutConnexion) {

        return pointDeVenteRepository
                .findByStatutConnexion(statutConnexion)
                .stream()
                .map(dtotMapper::fromPointDeVente)
                .toList();
    }
}