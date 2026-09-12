package org.sid.restaurationbackend.services;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sid.restaurationbackend.dtos.PlanDeSalleDTO;
import org.sid.restaurationbackend.dtos.RestaurantDTO;
import org.sid.restaurationbackend.entities.PlanDeSalle;
import org.sid.restaurationbackend.exceptions.PlanDeSalleNotFoundException;
import org.sid.restaurationbackend.mappers.RestaurantMapper;
import org.sid.restaurationbackend.repositories.PlanDeSalleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@Slf4j
public class PlanDeSalleServiceImpl
        implements PlanDeSalleService {

    private final RestaurantMapper dtoMapper;
    private final PlanDeSalleRepository planDeSalleRepository;




    @Override
    public PlanDeSalleDTO savePlanDeSalle(
            PlanDeSalleDTO planDeSalleDTO) {

        PlanDeSalle planDeSalle =
                dtoMapper.fromPlanDeSalleDTO(planDeSalleDTO);

        // Date automatique
        planDeSalle.setDateMiseAJour(new Date());

        PlanDeSalle savedPlanDeSalle =
                planDeSalleRepository.save(planDeSalle);

        log.info(
                "Plan de salle créé : {}",
                savedPlanDeSalle.getId_plan()
        );

        return dtoMapper.fromPlanDeSalle(
                savedPlanDeSalle
        );
    }




    @Override
    public PlanDeSalleDTO updatePlanDeSalle(
            Long id,
            PlanDeSalleDTO planDeSalleDTO)
            throws PlanDeSalleNotFoundException {

        PlanDeSalle planDeSalle =
                planDeSalleRepository.findById(id)
                        .orElseThrow(() ->
                                new PlanDeSalleNotFoundException(
                                        "Plan de salle not found"
                                )
                        );

        dtoMapper.updatePlanDeSalleFromDto(
                planDeSalleDTO,
                planDeSalle
        );


        planDeSalle.setDateMiseAJour(new Date());

        PlanDeSalle updatedPlanDeSalle =
                planDeSalleRepository.save(planDeSalle);

        return dtoMapper.fromPlanDeSalle(
                updatedPlanDeSalle
        );
    }




    @Override
    @Transactional(readOnly = true)
    public PlanDeSalleDTO getPlanDeSalle(
            Long id)
            throws PlanDeSalleNotFoundException {

        PlanDeSalle planDeSalle =
                planDeSalleRepository.findById(id)
                        .orElseThrow(() ->
                                new PlanDeSalleNotFoundException(
                                        "Plan de salle not found"
                                )
                        );

        return dtoMapper.fromPlanDeSalle(
                planDeSalle
        );
    }



    @Override
    public void deletePlanDeSalle(
            Long id)
            throws PlanDeSalleNotFoundException {

        PlanDeSalle planDeSalle =
                planDeSalleRepository.findById(id)
                        .orElseThrow(() ->
                                new PlanDeSalleNotFoundException(
                                        "Plan de salle not found"
                                )
                        );

        planDeSalleRepository.delete(planDeSalle);
    }



    @Override
    @Transactional(readOnly = true)
    public List<PlanDeSalleDTO> listPlanDeSalle() {

        return planDeSalleRepository
                .findAll()
                .stream()
                .map(dtoMapper::fromPlanDeSalle)
                .toList();
    }




    @Override
    @Transactional(readOnly = true)
    public List<PlanDeSalleDTO> listPlanDeSalleByRestaurant(
            RestaurantDTO restaurant) {

        return planDeSalleRepository
                .findByRestaurant(
                        dtoMapper.fromRestaurantDTO(restaurant)
                )
                .stream()
                .map(dtoMapper::fromPlanDeSalle)
                .toList();
    }



    @Override
    @Transactional(readOnly = true)
    public PlanDeSalleDTO getCurrentPlanDeSalleByRestaurant(
            RestaurantDTO restaurant)
            throws PlanDeSalleNotFoundException {

        PlanDeSalle planDeSalle =
                planDeSalleRepository
                        .findFirstByRestaurantOrderByDateMiseAJourDesc(
                                dtoMapper.fromRestaurantDTO(restaurant)
                        )
                        .orElseThrow(() ->
                                new PlanDeSalleNotFoundException(
                                        "Aucun plan de salle trouvé pour ce restaurant"
                                )
                        );

        return dtoMapper.fromPlanDeSalle(
                planDeSalle
        );
    }
}

