package org.sid.restaurationbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Ligne de la page "Présence" du Back Office : un employé, son statut
// pour le jour consulté (peut être null si rien n'a encore été
// marqué ce jour-là) et ses compteurs d'absences (mois courant /
// année courante) calculés côté serveur.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeuillePresenceDTO {

    private EmployeeDTO employee;

    private PresenceDTO presenceDuJour;

    private long absencesMoisCourant;

    private long absencesAnneeCourante;
}
