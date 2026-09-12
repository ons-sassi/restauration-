
        package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.PlanDeSalleDTO;
import org.sid.restaurationbackend.dtos.RestaurantDTO;
import org.sid.restaurationbackend.exceptions.PlanDeSalleNotFoundException;

import java.util.List;

public interface PlanDeSalleService {

    PlanDeSalleDTO savePlanDeSalle(
            PlanDeSalleDTO planDeSalleDTO
    );

    PlanDeSalleDTO updatePlanDeSalle(
            Long id,
            PlanDeSalleDTO planDeSalleDTO
    ) throws PlanDeSalleNotFoundException;

    PlanDeSalleDTO getPlanDeSalle(
            Long id
    ) throws PlanDeSalleNotFoundException;

    void deletePlanDeSalle(
            Long id
    ) throws PlanDeSalleNotFoundException;

    List<PlanDeSalleDTO> listPlanDeSalle();

    List<PlanDeSalleDTO> listPlanDeSalleByRestaurant(
            RestaurantDTO restaurant
    );

    PlanDeSalleDTO getCurrentPlanDeSalleByRestaurant(
            RestaurantDTO restaurant
    ) throws PlanDeSalleNotFoundException;
}

