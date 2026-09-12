package org.sid.restaurationbackend.exceptions;

public class PerformanceNotFoundException extends Exception{
    public PerformanceNotFoundException(String performanceNotFound) {
        super(performanceNotFound);
    }
}
