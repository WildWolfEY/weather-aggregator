package ru.home.weather.aggregator.exception;

public class CryptoTokenReadingException extends Exception {
    public CryptoTokenReadingException(String message, Exception e) {
        super(message, e);
    }
}
