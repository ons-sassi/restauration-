package org.sid.restaurationbackend.exceptions;

public class ModuleNotFoundException extends Exception {
    public ModuleNotFoundException(String moduleNotFound) {
        super(moduleNotFound);
    }
}
