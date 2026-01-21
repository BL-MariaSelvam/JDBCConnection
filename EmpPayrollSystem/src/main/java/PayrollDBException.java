package main.java;

public class PayrollDBException extends Exception {

    public PayrollDBException(String message) {
        super(message);
    }

    public PayrollDBException(String message, Throwable cause) {
        super(message, cause);
    }
}
