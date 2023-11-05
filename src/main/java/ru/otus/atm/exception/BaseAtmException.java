package ru.otus.atm.exception;

public class BaseAtmException extends Exception {

    public BaseAtmException() {
        super();
    }

    public BaseAtmException(String message) {
        super(message);
    }

    public BaseAtmException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseAtmException(Throwable cause) {
        super(cause);
    }

    protected BaseAtmException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
