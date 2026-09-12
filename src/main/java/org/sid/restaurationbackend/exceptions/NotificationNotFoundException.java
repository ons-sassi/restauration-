package org.sid.restaurationbackend.exceptions;

public class NotificationNotFoundException extends Exception {
    public NotificationNotFoundException(String notificationNotFound) {
        super(notificationNotFound);
    }
}
