package ru.home.weather.aggregator.exception;

public class CryptoKeyGeneratedException extends Exception {
    public CryptoKeyGeneratedException(String message, Exception e) {
        super(message, e);
    }
}
