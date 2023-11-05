package ru.otus.atm.exception;

public class CashSectionOverflowException extends BaseAtmException {
    public CashSectionOverflowException() {
        super();
    }

    public CashSectionOverflowException(String message) {
        super(message);
    }

    public CashSectionOverflowException(String message, Throwable cause) {
        super(message, cause);
    }

    public CashSectionOverflowException(Throwable cause) {
        super(cause);
    }

    protected CashSectionOverflowException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
