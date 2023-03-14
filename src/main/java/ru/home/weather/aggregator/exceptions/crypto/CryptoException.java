package ru.home.weather.aggregator.exceptions.crypto;

public class CryptoException extends Exception {
    public CryptoException(String message, Exception e) {
        super(message, e);
    }
}
