package org.sid.restaurationbackend.services;

import org.sid.restaurationbackend.dtos.DashboardDTO;

import java.time.LocalDate;

public interface DashboardService {

    DashboardDTO getDashboard(
            Long restaurantId,
            LocalDate date
    );
}