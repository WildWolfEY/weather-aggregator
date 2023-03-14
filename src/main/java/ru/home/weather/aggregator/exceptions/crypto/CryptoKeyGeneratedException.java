package ru.home.weather.aggregator.exceptions.crypto;

public class CryptoKeyGeneratedException extends CryptoException {
    public CryptoKeyGeneratedException(String message, Exception e) {
        super(message, e);
    }

    public CryptoKeyGeneratedException(Exception e) {
        super("Ошибка генерации секретного ключа", e);
    }
}
