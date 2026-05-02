package org.example.exceptions;

public class MaxWalletsExceededException extends RuntimeException {
    public MaxWalletsExceededException(String message) {
        super(message);
    }
}
