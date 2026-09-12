package org.sid.restaurationbackend.exceptions;

public class VersementSalaireNotFoundException extends Exception{
    public VersementSalaireNotFoundException(String versementSalaireNotFound) {
        super(versementSalaireNotFound);
    }
}
