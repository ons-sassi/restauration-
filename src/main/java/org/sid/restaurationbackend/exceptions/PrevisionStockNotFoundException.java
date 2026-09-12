package org.sid.restaurationbackend.exceptions;

public class PrevisionStockNotFoundException extends Exception {
    public PrevisionStockNotFoundException(String previsionDeStockNotFound) {
        super(previsionDeStockNotFound);
    }
}
