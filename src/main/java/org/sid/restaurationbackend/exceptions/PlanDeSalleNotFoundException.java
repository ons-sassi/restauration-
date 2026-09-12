package org.sid.restaurationbackend.exceptions;

public class PlanDeSalleNotFoundException extends Exception{
    public PlanDeSalleNotFoundException(String parameterDeCompteNotFound) {
        super(parameterDeCompteNotFound);
    }
}
