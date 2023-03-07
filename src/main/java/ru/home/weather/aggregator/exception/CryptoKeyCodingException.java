package ru.home.weather.aggregator.exception;

public class CryptoKeyCodingException extends Exception {
    public CryptoKeyCodingException(String message, Exception e) {
        super(message, e);
    }
}
