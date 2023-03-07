package ru.home.weather.aggregator.exception;

public class CryptoTokenSavingException extends Exception {
    public CryptoTokenSavingException(String message, Exception e) {
        super(message, e);
    }
}
