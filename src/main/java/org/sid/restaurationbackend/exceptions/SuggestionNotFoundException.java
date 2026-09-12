package org.sid.restaurationbackend.exceptions;

public class SuggestionNotFoundException extends Exception {
    public SuggestionNotFoundException(String suggestionNotFound) {
        super(suggestionNotFound);
    }
}
