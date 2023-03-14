package ru.home.weather.aggregator.exceptions.crypto;

public class CryptoKeyReadingException extends CryptoException {
    public CryptoKeyReadingException(String message, Exception e) {
        super(message, e);
    }

    public CryptoKeyReadingException(Exception e) {
        super("Ошибка чтения секретного ключа",e);
    }
}
