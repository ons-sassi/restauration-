package org.sid.restaurationbackend.exceptions;

public class RoleNotFoundException extends Exception{
    public RoleNotFoundException(String roleNotFound) {
        super(roleNotFound);
    }
}
