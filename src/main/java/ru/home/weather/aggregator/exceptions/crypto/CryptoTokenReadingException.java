package ru.home.weather.aggregator.exceptions.crypto;

public class CryptoTokenReadingException extends CryptoException {
    public CryptoTokenReadingException(String message, Exception e) {
        super(message, e);
    }
    public CryptoTokenReadingException(Exception e) {
        super("Ошибка чтения зашифрованных данных", e);
    }
}
