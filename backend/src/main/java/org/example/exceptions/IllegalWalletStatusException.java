package org.example.exceptions;

public class IllegalWalletStatusException extends RuntimeException {
    public IllegalWalletStatusException(String message) {
        super(message);
    }
}
