package ru.home.weather.aggregator.exception;

public class CryptoKeyReadingException extends Exception {
    public CryptoKeyReadingException(String message, Exception e) {
        super(message, e);
    }
}
