package ru.otus.exception;

public class AtmException extends Exception{
    public AtmException(String message) {
        super(message);
    }

    public AtmException(String message, Throwable cause) {
        super(message, cause);
    }
}
